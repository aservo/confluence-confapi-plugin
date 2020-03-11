package de.aservo.atlassian.confluence.confapi.service;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType;
import com.atlassian.applinks.api.application.bitbucket.BitbucketApplicationType;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.applinks.api.application.crowd.CrowdApplicationType;
import com.atlassian.applinks.api.application.fecru.FishEyeCrucibleApplicationType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import de.aservo.atlassian.confluence.confapi.model.ApplicationLinkBean;
import de.aservo.atlassian.confluence.confapi.model.ApplicationLinkTypes;
import de.aservo.atlassian.confluence.confapi.model.DefaultAuthenticationScenario;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The type Application link service.
 */
@ExportAsService
@ConfluenceComponent
public class ApplicationLinkService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationLinkService.class);

    private final MutatingApplicationLinkService applicationLinkService;
    private final TypeAccessor typeAccessor;

    /**
     * Instantiates a new Application link service.
     *
     * @param applicationLinkService the application link service
     * @param typeAccessor           the type accessor
     */
    public ApplicationLinkService(@ComponentImport MutatingApplicationLinkService applicationLinkService,
                                  @ComponentImport TypeAccessor typeAccessor) {
        this.applicationLinkService = applicationLinkService;
        this.typeAccessor = typeAccessor;
    }

    /**
     * Gets application links.
     *
     * @return the application links
     */
    public List<ApplicationLinkBean> getApplicationLinks() {
        Iterable<ApplicationLink> applicationLinksIterable = applicationLinkService.getApplicationLinks();
        return StreamSupport.stream(applicationLinksIterable.spliterator(), false)
                .map(ApplicationLinkBean::new).collect(Collectors.toList());
    }

    /**
     * Adds a new application link. NOTE: existing application links with the same type, e.g. "JIRA" will be
     * removed before adding the new configuration.
     *
     * @param linkBean the link bean
     * @return the added application ,link
     * @throws URISyntaxException                   the uri syntax exception
     * @throws ManifestNotFoundException            the manifest not found exception
     * @throws AuthenticationConfigurationException the authentication configuration exception
     */
    public ApplicationLink addApplicationLink(ApplicationLinkBean linkBean) throws URISyntaxException, ManifestNotFoundException, AuthenticationConfigurationException {
        //preparations
        BeanValidationService.validate(linkBean);
        ApplicationLinkDetails linkDetails = linkBean.toApplicationLinkDetails();
        ApplicationType applicationType = buildApplicationType(linkBean.getLinkType());

        //check if there is already an application link of supplied type and if yes, remove it
        Class<? extends ApplicationType> appType = applicationType != null ? applicationType.getClass() : null;
        ApplicationLink primaryApplicationLink = applicationLinkService.getPrimaryApplicationLink(appType);
        if (primaryApplicationLink != null) {
            log.info("An existing application link configuration '{}' was found and is removed now before adding the new configuration", primaryApplicationLink.getName());
            applicationLinkService.deleteApplicationLink(primaryApplicationLink);
        }

        //add new application link
        ApplicationLink applicationLink = applicationLinkService.createApplicationLink(applicationType, linkDetails);
        applicationLinkService.configureAuthenticationForApplicationLink(applicationLink,
                new DefaultAuthenticationScenario(), linkBean.getUsername(), linkBean.getPassword());

        return applicationLink;
    }

    private ApplicationType buildApplicationType(ApplicationLinkTypes linkType) {
        switch (linkType) {
            case BAMBOO:
                return typeAccessor.getApplicationType(BambooApplicationType.class);
            case JIRA:
                return typeAccessor.getApplicationType(JiraApplicationType.class);
            case BITBUCKET:
                return typeAccessor.getApplicationType(BitbucketApplicationType.class);
            case CONFLUENCE:
                return typeAccessor.getApplicationType(ConfluenceApplicationType.class);
            case FISHEYE:
                return typeAccessor.getApplicationType(FishEyeCrucibleApplicationType.class);
            case CROWD:
                return typeAccessor.getApplicationType(CrowdApplicationType.class);
            default:
                throw new NotImplementedException("application type '" + linkType + "' not implemented");
        }
    }
}
