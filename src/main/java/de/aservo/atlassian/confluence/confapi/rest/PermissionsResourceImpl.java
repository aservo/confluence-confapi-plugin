package de.aservo.atlassian.confluence.confapi.rest;

import com.sun.jersey.spi.container.ResourceFilters;
import de.aservo.atlassian.confluence.confapi.filter.AdminOnlyResourceFilter;
import de.aservo.atlassian.confluence.confapi.model.PermissionAnonymousAccessBean;
import de.aservo.atlassian.confluence.confapi.rest.api.PermissionsResource;
import de.aservo.atlassian.confluence.confapi.rest.api.PermissionsService;
import de.aservo.confapi.commons.constants.ConfAPI;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(ConfAPI.PERMISSIONS)
@Produces(MediaType.APPLICATION_JSON)
@ResourceFilters(AdminOnlyResourceFilter.class)
@Component
public class PermissionsResourceImpl implements PermissionsResource {

    private final PermissionsService permissionsService;

    @Inject
    public PermissionsResourceImpl(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @Override
    public Response getPermissionAnonymousAccess() {
        return Response.ok(permissionsService.getPermissionAnonymousAccess()).build();
    }

    @Override
    public Response setPermissionAnonymousAccess(
            @NotNull final PermissionAnonymousAccessBean accessBean) {
        return Response.ok(permissionsService.setPermissionAnonymousAccess(accessBean)).build();
    }
}
