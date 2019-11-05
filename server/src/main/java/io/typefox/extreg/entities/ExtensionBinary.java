/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ExtensionBinary {

    @Id
    @GeneratedValue
    private long id;

    @OneToOne
    private ExtensionVersion extension;

    private byte[] content;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ExtensionVersion getExtension() {
		return extension;
	}

	public void setExtension(ExtensionVersion extension) {
		this.extension = extension;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}