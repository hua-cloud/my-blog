package com.example.category;

import com.example.domain.content.adapter.query.ICategoryQuery;
import com.example.domain.content.adapter.query.IContentReferenceQuery;
import com.example.domain.content.adapter.repository.ICategoryRepository;
import com.example.domain.content.service.CategoryApplicationService;
import com.example.domain.content.service.command.SaveCategoryCommand;
import com.example.domain.content.model.entity.Category;
import com.example.types.core.AppException;
import com.example.types.enums.ResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CategoryApplicationServiceTest {
    private ICategoryRepository repository;
    private IContentReferenceQuery references;
    private CategoryApplicationService service;

    @BeforeEach
    void setUp() {
        repository = mock(ICategoryRepository.class);
        references = mock(IContentReferenceQuery.class);
        service = new CategoryApplicationService(repository, references, mock(ICategoryQuery.class));
    }

    @Test
    void createNormalizesNameAndDefaultsSortOrder() {
        when(repository.create(any(), eq(9L))).thenReturn(7L);
        assertEquals(7L, service.create(new SaveCategoryCommand("  Java  ", null, null), 9L));
        verify(repository).create(argThat(c -> c.getName().equals("Java") && c.getSortOrder() == 0), eq(9L));
    }

    @Test
    void rejectsBlankOrOversizedNamesAndDescriptions() {
        assertThrows(IllegalArgumentException.class, () -> Category.create("  ", null, 0));
        assertThrows(IllegalArgumentException.class, () -> Category.create("a".repeat(65), null, 0));
        assertThrows(IllegalArgumentException.class, () -> Category.create("ok", "x".repeat(256), 0));
        assertEquals("😀".repeat(64), Category.create("😀".repeat(64), null, 0).getName());
    }

    @Test
    void duplicateNameIsBusinessError() {
        when(repository.existsByName("Java", null)).thenReturn(true);
        AppException error = assertThrows(AppException.class,
                () -> service.create(new SaveCategoryCommand(" Java ", null, 0), 9L));
        assertEquals(ResultStatus.CATEGORY_NAME_EXISTS, error.getResultStatus());
        verify(repository, never()).create(any(), anyLong());
    }

    @Test
    void updateLocksBeforeReadingAndCanClearDescription() {
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(Category.restore(7L, "Old", "old", 4)));
        service.update(7L, new SaveCategoryCommand(" New ", null, -1), 9L);
        var order = inOrder(repository);
        order.verify(repository).findByIdForUpdate(7L);
        order.verify(repository).existsByName("New", 7L);
        order.verify(repository).update(argThat(c -> c.getDescription() == null && c.getSortOrder() == -1), eq(9L));
    }

    @Test
    void missingCategoryCannotBeUpdated() {
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.empty());
        AppException error = assertThrows(AppException.class,
                () -> service.update(7L, new SaveCategoryCommand("Name", null, 0), 9L));
        assertEquals(ResultStatus.CATEGORY_NOT_FOUND, error.getResultStatus());
    }

    @Test
    void referencedCategoryCannotBeDeleted() {
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(Category.restore(7L, "Java", null, 0)));
        when(references.existsArticleUsingCategory(7L)).thenReturn(true);
        AppException error = assertThrows(AppException.class, () -> service.delete(7L));
        assertEquals(ResultStatus.CATEGORY_IN_USE, error.getResultStatus());
        var order = inOrder(repository, references);
        order.verify(repository).findByIdForUpdate(7L);
        order.verify(references).existsArticleUsingCategory(7L);
        verify(repository, never()).delete(anyLong());
    }

    @Test
    void unreferencedCategoryIsDeletedAndMissingDeleteIsIdempotent() {
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(Category.restore(7L, "Java", null, 0)));
        service.delete(7L);
        verify(repository).delete(7L);
        when(repository.findByIdForUpdate(8L)).thenReturn(Optional.empty());
        service.delete(8L);
        verify(references, never()).existsArticleUsingCategory(8L);
        verify(repository, never()).delete(8L);
    }

    @Test
    void rejectsInvalidIdentifiersBeforePersistence() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(0L));
        assertThrows(IllegalArgumentException.class, () -> service.create(new SaveCategoryCommand("Java", null, 0), -1L));
        verifyNoInteractions(repository, references);
    }

    @Test
    void deleteUsesReadCommittedAndRollbackForCheckedExceptions() throws Exception {
        Transactional tx = CategoryApplicationService.class.getMethod("delete", Long.class).getAnnotation(Transactional.class);
        assertNotNull(tx);
        assertEquals(Isolation.READ_COMMITTED, tx.isolation());
        assertArrayEquals(new Class<?>[]{Exception.class}, tx.rollbackFor());
    }

    @Test
    void transactionProxyCommitsSuccessAndRollsBackReferenceFailure() {
        var manager = mock(org.springframework.transaction.PlatformTransactionManager.class);
        var transaction = new org.springframework.transaction.support.SimpleTransactionStatus();
        when(manager.getTransaction(any())).thenReturn(transaction);
        var factory = new org.springframework.aop.framework.ProxyFactory(service);
        factory.addAdvice(new org.springframework.transaction.interceptor.TransactionInterceptor(manager,
                new org.springframework.transaction.annotation.AnnotationTransactionAttributeSource()));
        var proxy = (CategoryApplicationService) factory.getProxy();
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(Category.restore(7L, "Java", null, 0)));
        proxy.delete(7L);
        verify(manager).commit(transaction);
        when(references.existsArticleUsingCategory(7L)).thenReturn(true);
        assertThrows(AppException.class, () -> proxy.delete(7L));
        verify(manager).rollback(transaction);
    }
}
