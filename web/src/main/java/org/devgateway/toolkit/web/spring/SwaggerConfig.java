package org.devgateway.toolkit.web.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;

import static springfox.documentation.builders.PathSelectors.regex;

@ConditionalOnProperty("apiDocs")
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket ocDashboardsApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("1ocDashboardsApi")
                .apiInfo(ocDashboardsApiInfo())
                .select().apis(RequestHandlerSelectors.any()).paths(regex("/api/.*"))
                .build();
        docket.ignoredParameterTypes(ModelAndView.class, RedirectAttributes.class,
                HttpServletRequest.class, HttpServletResponse.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        return docket;

    }

    @Bean
    public Docket manageApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("2manageApi")
                .select().apis(RequestHandlerSelectors.any()).paths(regex("/manage/.*")).build();
        docket.ignoredParameterTypes(ModelAndView.class, RedirectAttributes.class,
                HttpServletRequest.class, HttpServletResponse.class);
        docket.directModelSubstitute(ZonedDateTime.class, java.util.Date.class);
        return docket;
    }

    private ApiInfo ocDashboardsApiInfo() {
        return new ApiInfoBuilder().title("Open Contracting Dashboards API")
                .description("These endpoints are used to feed the open contracting dashboard").license("MIT License")
                .licenseUrl("https://opensource.org/licenses/MIT").version("1.0").build();
    }

}