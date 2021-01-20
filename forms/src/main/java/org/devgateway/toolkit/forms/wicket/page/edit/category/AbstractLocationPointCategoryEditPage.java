/*******************************************************************************
 * Copyright (c) 2015 Development Gateway, Inc and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * https://opensource.org/licenses/MIT
 *
 * Contributors:
 * Development Gateway - initial API and implementation
 *******************************************************************************/
package org.devgateway.toolkit.forms.wicket.page.edit.category;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.devgateway.toolkit.forms.wicket.components.form.TextFieldBootstrapFormComponent;
import org.devgateway.toolkit.forms.wicket.components.util.ComponentUtil;
import org.devgateway.toolkit.persistence.dao.categories.LocationPointCategory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.Objects;

/**
 * @author mpostelnicu
 */

public abstract class AbstractLocationPointCategoryEditPage<T extends LocationPointCategory>
        extends AbstractCategoryEditPage<T> {

    protected TextFieldBootstrapFormComponent<Double> x;
    protected TextFieldBootstrapFormComponent<Double> y;

    public AbstractLocationPointCategoryEditPage(final PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        x = ComponentUtil.addDoubleField(editForm, "x");
        x.required();
        y = ComponentUtil.addDoubleField(editForm, "y");
        y.required();
    }

    @Override
    protected void afterLoad(IModel<T> entityModel) {
        super.afterLoad(entityModel);
        if (!Objects.isNull(entityModel.getObject().getLocationPoint())) {
            entityModel.getObject().setX(entityModel.getObject().getLocationPoint().getX());
            entityModel.getObject().setY(entityModel.getObject().getLocationPoint().getY());
        }
    }

    @Override
    protected void beforeSaveEntity(T saveable) {
        super.beforeSaveEntity(saveable);
        GeometryFactory gf = new GeometryFactory();
        saveable.setLocationPoint(gf.createPoint(new Coordinate(saveable.getX(), saveable.getY())));
    }
}
