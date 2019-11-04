/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.upload;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PackageDeserializerTest {

    private Jsonb createJsonb() {
        var config = new JsonbConfig().withDeserializers(new PackageDeserializer());
        return JsonbBuilder.create(config);
    }

    @Test
    public void testTodoTree() throws Exception {
        var stream = getClass().getResourceAsStream("todo-tree.json");
        var jsonb = createJsonb();
        Package result = jsonb.fromJson(stream, Package.class);
        assertNotNull(result.repository);
        assertEquals("todo-tree", result.name);
        assertEquals("https://github.com/Gruntfuggly/todo-tree", result.repository.url);
    }

}