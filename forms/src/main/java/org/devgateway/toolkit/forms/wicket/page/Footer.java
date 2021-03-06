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
package org.devgateway.toolkit.forms.wicket.page;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Footer extends Panel {

    private static final long serialVersionUID = 1L;

    /**
     * Construct.
     *
     * @param markupId The components markup id.
     */
    public Footer(final String markupId) {
        super(markupId);

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("version.properties");
        InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream("branch.properties");
        Properties prop = new Properties();
        try {
            prop.load(inputStream);
            prop.load(inputStream2);
            inputStream.close();
            inputStream2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(new Label("makueni-version",
                new StringResourceModel("version", this)
                        .setParameters(prop.getProperty("makueni.version"))));
        add(new Label("makueni-branch", Model.of(prop.getProperty("makueni.branch"))));
    }
}
