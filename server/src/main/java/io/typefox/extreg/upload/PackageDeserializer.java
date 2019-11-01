/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.upload;

import java.lang.reflect.Type;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

public class PackageDeserializer implements JsonbDeserializer<Package> {

	@Override
	public Package deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        var packag = new Package();
        JsonParser.Event next;
        while ((next = parser.next()) != JsonParser.Event.END_OBJECT) {
            if (next == JsonParser.Event.KEY_NAME) {
                String jsonKeyName = parser.getString();
                parser.next();
                switch (jsonKeyName) {
                    default:
                        parseDefaultField(jsonKeyName, packag, parser);
                }
            }
        }
		return packag;
	}

    private void parseDefaultField(String jsonKeyName, Package packag, JsonParser parser) {
        try {
            var field = Package.class.getDeclaredField(jsonKeyName);
            var value = ""; // TODO
            field.set(packag, value);
        } catch (NoSuchFieldException exc) {
            // Ignore the JSON value
		} catch (IllegalAccessException exc) {
			throw new RuntimeException(exc);
		}
    }

}