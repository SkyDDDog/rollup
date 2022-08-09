package com.lyd.config;

import com.fasterxml.classmate.TypeResolver;
import com.lyd.controller.dto.UserDTO;
import org.apache.commons.compress.utils.Sets;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.Operation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.ApiListingScannerPlugin;
import springfox.documentation.spi.service.contexts.DocumentationContext;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;

import java.util.*;

//@Component
public class SwaggerAddtion implements ApiListingScannerPlugin {
    /**
     * Implement this method to manually add ApiDescriptions
     * 实现此方法可手动添加ApiDescriptions
     *
     * @param context - Documentation context that can be used infer documentation context
     * @return List of {@link ApiDescription}
     * @see ApiDescription
     */
    @Override
    public List<ApiDescription> apply(DocumentationContext context) {
        Operation usernamePasswordOperation = new OperationBuilder(new CachingOperationNameGenerator())
                .method(HttpMethod.POST)
                .summary("邮箱密码登录")
                .notes("email/password登录")
                .consumes(Sets.newHashSet(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) // 接收参数格式
                .produces(Sets.newHashSet(MediaType.APPLICATION_JSON_VALUE)) // 返回参数格式
                .tags(Sets.newHashSet("登录"))
                .parameters(Arrays.asList(
                        new ParameterBuilder()
                                .description("123")
                                .type(new TypeResolver().resolve(UserDTO.class))
                                .name("userLoginDTO")
                                .parameterType("body")
                                .parameterAccess("access")
                                .required(true)
                                .modelRef(new ModelRef("com.lyd.controller.dto.UserLoginDTO"))

                                .build()
                ))
                .responseMessages(Collections.singleton(
                        new ResponseMessageBuilder().code(200).message("请求成功")
                                .responseModel(new ModelRef(
                                        "com.lyd.common.Result")
                                ).build()))
                .build();

        ApiDescription loginApiDescription = new ApiDescription("/user/login", "登录接口",
                Arrays.asList(usernamePasswordOperation), false);

        return Arrays.asList(loginApiDescription);
    }

    /**
     * 是否使用此插件
     *
     * @param documentationType swagger文档类型
     * @return true 启用
     */
    @Override
    public boolean supports(DocumentationType documentationType) {
        return DocumentationType.SWAGGER_2.equals(documentationType);
    }
}