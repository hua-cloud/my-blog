package com.example.category;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.example.domain.content.service.CategoryApplicationService;
import com.example.domain.content.query.model.CategoryQuery;
import com.example.domain.content.query.model.CategoryView;
import com.example.trigger.http.CategoryController;
import com.example.trigger.http.advice.GlobalExceptionHandler;
import com.example.types.core.AppException;
import com.example.types.enums.ResultStatus;
import com.example.types.page.PageResult;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CategoryControllerTest {
    @org.springframework.context.annotation.Configuration
    @org.springframework.web.servlet.config.annotation.EnableWebMvc
    @org.springframework.context.annotation.Import({com.example.config.SaTokenConfig.class,
            CategoryController.class, GlobalExceptionHandler.class})
    static class SecuredWebConfig {
        @org.springframework.context.annotation.Bean
        CategoryApplicationService categoryService() { return mock(CategoryApplicationService.class); }
    }

    private CategoryApplicationService service;
    private MockMvc mvc;
    private StpLogic original;

    @BeforeEach
    void setUp() {
        service = mock(CategoryApplicationService.class);
        mvc = MockMvcBuilders.standaloneSetup(new CategoryController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        original = StpUtil.getStpLogic();
        StpUtil.setStpLogic(new StpLogic("login") {
            @Override public long getLoginIdAsLong() { return 9L; }
        });
    }

    @AfterEach
    void tearDown() { StpUtil.setStpLogic(original); }

    @Test
    void createUsesAuthenticatedOperatorAndReturnsIdentifier() throws Exception {
        when(service.create(any(), eq(9L))).thenReturn(7L);
        mvc.perform(post("/admin/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"sortOrder\":2,\"createBy\":999}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data").value(7));
        verify(service).create(argThat(c -> c.name().equals("Java") && c.sortOrder() == 2), eq(9L));
    }

    @Test
    void validatesBodyBeforeCallingUseCase() throws Exception {
        mvc.perform(post("/admin/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(jsonPath("$.code").value(400));
        mvc.perform(post("/admin/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\",\"sortOrder\":\"abc\"}"))
                .andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    void updateAllowsClearingDescriptionAndReturnsSuccess() throws Exception {
        mvc.perform(put("/admin/categories/7").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\",\"description\":null}"))
                .andExpect(jsonPath("$.code").value(1000));
        verify(service).update(eq(7L), argThat(c -> c.description() == null), eq(9L));
    }

    @Test
    void detailAndPageUseExistingResponseProtocol() throws Exception {
        when(service.detail(7L)).thenReturn(new CategoryView(7L, "Java", null, 2));
        mvc.perform(get("/admin/categories/7"))
                .andExpect(jsonPath("$.data.name").value("Java"));
        when(service.page(any(CategoryQuery.class))).thenReturn(PageResult.of(List.of(), 2L, 3, 5));
        mvc.perform(get("/admin/categories").param("pageNum", "3").param("pageSize", "5").param("keyword", " Java "))
                .andExpect(jsonPath("$.data.records").isEmpty())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.pageNum").value(3));
        verify(service).page(argThat(q -> q.getPageNum() == 3 && q.getPageSize() == 5 && q.getKeyword().equals("Java")));
    }

    @Test
    void invalidPaginationIsRejected() throws Exception {
        mvc.perform(get("/admin/categories").param("pageNum", "0"))
                .andExpect(jsonPath("$.code").value(400));
        mvc.perform(get("/admin/categories").param("pageSize", "101"))
                .andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    void malformedPathIdentifierIsParameterError() throws Exception {
        mvc.perform(get("/admin/categories/not-a-number"))
                .andExpect(jsonPath("$.code").value(400));
        verifyNoInteractions(service);
    }

    @Test
    void realInterceptorRejectsUnauthenticatedCategoryAccess() throws Exception {
        var originalContext = cn.dev33.satoken.SaManager.getSaTokenContext();
        cn.dev33.satoken.SaManager.setSaTokenContext(new cn.dev33.satoken.spring.SaTokenContextForSpringInJakartaServlet());
        try (var context = new org.springframework.web.context.support.AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new org.springframework.mock.web.MockServletContext());
            context.register(SecuredWebConfig.class);
            context.refresh();
            var securedMvc = MockMvcBuilders.webAppContextSetup(context).build();
            for (var method : List.of("GET", "POST", "PUT", "DELETE")) {
                String path = "POST".equals(method) ? "/admin/categories" : "/admin/categories/7";
                securedMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .request(org.springframework.http.HttpMethod.valueOf(method), path)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\"}"))
                        .andExpect(jsonPath("$.code").value(401));
            }
            verifyNoInteractions(context.getBean(CategoryApplicationService.class));
        } finally {
            cn.dev33.satoken.SaManager.setSaTokenContext(originalContext);
        }
    }

    @Test
    void referencedDeleteAndDuplicateNameReturnBusinessErrors() throws Exception {
        doThrow(new AppException(ResultStatus.CATEGORY_IN_USE)).when(service).delete(7L);
        mvc.perform(delete("/admin/categories/7"))
                .andExpect(jsonPath("$.code").value(3003))
                .andExpect(jsonPath("$.msg").value("该分类下还有文章，无法删除"));
        when(service.create(any(), anyLong())).thenThrow(new AppException(ResultStatus.CATEGORY_NAME_EXISTS));
        mvc.perform(post("/admin/categories").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\"}"))
                .andExpect(jsonPath("$.code").value(3002));
    }
}
