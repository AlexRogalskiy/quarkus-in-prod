package org.acme.legume.resource;

import org.acme.legume.data.LegumeItem;
import org.acme.legume.data.LegumeNew;
import org.acme.legume.model.Legume;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
public class LegumeResource implements LegumeApi {

    @Inject
    EntityManager manager;

    @Transactional
    public Response provision() {
        final Legume carrot = Legume.builder()
                .name("Carrot")
                .description("Root vegetable, usually orange")
                .build();
        final Legume zucchini = Legume.builder()
                .name("Zucchini")
                .description("Summer squash")
                .build();
        return Response.status(CREATED).entity(asList(
                manager.merge(carrot),
                manager.merge(zucchini))).build();
    }

    @Transactional
    public Response add(@Valid final LegumeNew legumeNew) {
        final Legume legume = Legume.builder()
                .name(legumeNew.getName())
                .description((legumeNew.getDescription()))
                .build();
        final Legume addedLegume = manager.merge(legume);
        return Response.status(CREATED).entity(addedLegume).build();
    }

    @Transactional
    public Response delete(@NotEmpty final String legumeId) {
        return find(legumeId)
                .map(legume -> {
                    manager.remove(legume);
                    return Response.status(NO_CONTENT).build();
                })
                .orElse(Response.status(NOT_FOUND).build());
    }

    @Fallback(fallbackMethod = "fallback")
    @Timeout(500)
    public List<Legume> list() {
        return manager.createQuery("SELECT l FROM Legume l").getResultList();
    }

    /**
     * To be used in case of exception or timeout
     *
     * @return a list of alternative legumes.
     */
    public List<LegumeItem> fallback() {
        return asList(LegumeItem.builder()
                .name("Failed Legume")
                .description("Fallback answer due to timeout")
                .build());
    }

    private Optional<Legume> find(final String legumeId) {
        return Optional.ofNullable(manager.find(Legume.class, legumeId));
    }
}
