package com.example.category;

import com.example.domain.content.model.entity.Category;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.infrastructure.adapter.query.content.CategoryQueryAdapter;
import com.example.infrastructure.adapter.repository.content.CategoryRepository;
import com.example.infrastructure.persistence.mapper.content.ICategoryReferenceMapper;
import com.example.infrastructure.persistence.mapper.content.IBlogCategoryMapper;
import com.example.infrastructure.persistence.po.CategoryPo;
import com.example.types.core.AppException;
import com.example.types.enums.ResultStatus;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CategoryRepositoryTest {
    @Test
    void generatedUpdateSqlClearsDescriptionWithoutOverwritingCreateAudit() {
        var configuration = new MybatisConfiguration();
        configuration.addMapper(IBlogCategoryMapper.class);
        new com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder().build(configuration);
        CategoryPo po = new CategoryPo();
        po.setId(1L);
        po.setName("Java");
        po.setSortOrder(2);
        po.setUpdateBy(9L);
        var statement = configuration.getMappedStatement(IBlogCategoryMapper.class.getName() + ".updateById");
        String sql = statement.getBoundSql(java.util.Map.of("et", po)).getSql().replaceAll("\\s+", "").toLowerCase();
        assertTrue(sql.contains("description=?"));
        assertTrue(sql.contains("update_by=?"));
        assertFalse(sql.contains("create_by="));
        assertFalse(sql.contains("create_time="));
    }

    @Test
    void databaseUniqueConflictsAreTranslatedOnCreateAndUpdate() {
        var mapper = mock(IBlogCategoryMapper.class);
        var repository = new CategoryRepository(mapper);
        doThrow(new DuplicateKeyException("unique")).when(mapper).insert(any(CategoryPo.class));
        AppException createError = assertThrows(AppException.class,
                () -> repository.create(Category.create("Java", null, 0), 8L));
        assertEquals(ResultStatus.CATEGORY_NAME_EXISTS, createError.getResultStatus());
        doThrow(new DuplicateKeyException("unique")).when(mapper).updateById(any(CategoryPo.class));
        AppException updateError = assertThrows(AppException.class,
                () -> repository.update(Category.restore(1L, "Java", null, 0), 8L));
        assertEquals(ResultStatus.CATEGORY_NAME_EXISTS, updateError.getResultStatus());
    }

    @Test
    void writesExplicitOperatorAndPreservesCreateAuditOnUpdate() throws Exception {
        var mapper = mock(IBlogCategoryMapper.class);
        var repository = new CategoryRepository(mapper);
        when(mapper.insert(any(CategoryPo.class))).thenAnswer(call -> {
            CategoryPo po = call.getArgument(0);
            assertEquals(8L, po.getCreateBy());
            assertEquals(8L, po.getUpdateBy());
            assertNotNull(po.getCreateTime());
            po.setId(3L);
            return 1;
        });
        assertEquals(3L, repository.create(Category.create("Java", null, 0), 8L));
        repository.update(Category.restore(3L, "New", null, 2), 9L);
        verify(mapper).updateById(argThat((CategoryPo po) -> po.getCreateBy() == null
                && po.getUpdateBy().equals(9L) && po.getDescription() == null));
        assertEquals(FieldStrategy.ALWAYS, CategoryPo.class.getDeclaredField("description")
                .getAnnotation(TableField.class).updateStrategy());
    }

    @Test
    void lockAndReferenceSqlUseCorrectTablesAndAllArticleStates() throws Exception {
        String lockSql = String.join(" ", IBlogCategoryMapper.class.getMethod("selectByIdForUpdate", Long.class)
                .getAnnotation(Select.class).value()).toLowerCase();
        String referenceSql = String.join(" ", ICategoryReferenceMapper.class.getMethod("existsArticleUsingCategory", Long.class)
                .getAnnotation(Select.class).value()).toLowerCase();
        assertTrue(lockSql.contains("for update"));
        assertTrue(lockSql.contains("blog_category"));
        assertTrue(referenceSql.contains("blog_article"));
        assertTrue(referenceSql.contains("category_id"));
        assertFalse(referenceSql.contains("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void queryUsesPaginationStableSortingAndLiteralKeyword() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), CategoryPo.class);
        var mapper = mock(IBlogCategoryMapper.class);
        when(mapper.selectPage(any(Page.class), any(Wrapper.class))).thenAnswer(call -> {
            Page<CategoryPo> page = call.getArgument(0);
            Wrapper<CategoryPo> wrapper = call.getArgument(1);
            assertEquals(3, page.getCurrent());
            assertEquals(5, page.getSize());
            String sql = wrapper.getSqlSegment().toLowerCase();
            assertTrue(sql.contains("sort_order asc,id asc"));
            assertTrue(sql.contains("locate("));
            assertFalse(sql.contains("status"));
            page.setTotal(2);
            return page;
        });
        CategoryQuery query = new CategoryQuery();
        query.setPageNum(3);
        query.setPageSize(5);
        query.setKeyword("  100%  ");
        var result = new CategoryQueryAdapter(mapper).page(query);
        assertEquals(2L, result.getTotal());
        assertEquals(3, result.getPageNum());
        assertTrue(result.getRecords().isEmpty());
    }
}
