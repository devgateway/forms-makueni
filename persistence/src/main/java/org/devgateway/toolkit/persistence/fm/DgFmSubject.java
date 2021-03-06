package org.devgateway.toolkit.persistence.fm;

import org.devgateway.toolkit.persistence.fm.entity.DgFeature;
import org.devgateway.toolkit.persistence.fm.service.DgFmService;
import org.springframework.util.StringUtils;

import java.util.function.BooleanSupplier;

/**
 * Base interface of any java object that needs FM behavior
 *
 * @author mpostelnicu
 */
public interface DgFmSubject {

    default Boolean isFmEnabled() {
        return isFmAttached() ? getFmService().isFeatureEnabled(getFmName()) : FmConstants.DEFAULT_ENABLED;
    }

    default Boolean isFmVisible() {
        return isFmAttached() ? getFmService().isFeatureVisible(getFmName()) : FmConstants.DEFAULT_VISIBLE;
    }

    void setFmName(String fmName);

    String getFmName();

    default boolean isFmAttached() {
        return getFmName() != null;
    }

    DgFmService getFmService();

    /**
     * Attaches FM behavior to current component. Attaching means assigning a fmName to the current object.
     * This also marks the object as an user for this feature by adding it to the attached log
     *
     * @param fmName
     */
    default void attachFm(String fmName) {
        if (!getFmService().isFmActive() || isFmAttached()) {
            return;
        }
        if (StringUtils.isEmpty(fmName)) {
            throw new RuntimeException("Cannot attach to empty fmName!");
        }
        getFmService().getFeature(fmName).addAttachedLog(this);
        setFmName(fmName);
    }

    /**
     * combines testing for {@link DgFeature#getEnabled()} with the enabled supplier from the component.
     *
     * @param enabledSupplier
     * @return
     */
    default boolean isFmEnabled(BooleanSupplier enabledSupplier) {
        return enabledSupplier.getAsBoolean() && isFmEnabled();
    }

    /**
     * combines testing for {@link DgFeature#getVisible()} with the visible supplier from the component.
     *
     * @param visibleSupplier
     * @return
     */
    default boolean isFmVisible(BooleanSupplier visibleSupplier) {
        return visibleSupplier.getAsBoolean() && isFmVisible();
    }
}
