/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.entities;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class ExtensionVersion {

    @Id
    @GeneratedValue
    long id;

    @ManyToOne
    Extension extension;

    @OneToOne(mappedBy = "latest", fetch = FetchType.LAZY)
    Extension latestInverse;

    String version;

    boolean preview;

    LocalDateTime timestamp;

    String extensionFileName;

    String iconFileName;

    String readmeFileName;

    String displayName;

    @Column(length=2048)
    String description;

    @ElementCollection
    List<String> categories;

    @ElementCollection
    List<String> tags;

    String license;

    String homepage;

    String repository;

    String bugs;

    String markdown;

    String galleryColor;

    String galleryTheme;

    String qna;

    @ManyToMany
    List<Extension> dependencies;

    @ManyToMany
    List<Extension> bundledExtensions;


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Extension getExtension() {
		return extension;
	}

	public void setExtension(Extension extension) {
		this.extension = extension;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getExtensionFileName() {
		return extensionFileName;
	}

	public void setExtensionFileName(String extensionFileName) {
		this.extensionFileName = extensionFileName;
	}

	public String getIconFileName() {
		return iconFileName;
	}

	public void setIconFileName(String iconFileName) {
		this.iconFileName = iconFileName;
	}

	public String getReadmeFileName() {
		return readmeFileName;
	}

	public void setReadmeFileName(String readmeFileName) {
		this.readmeFileName = readmeFileName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getBugs() {
		return bugs;
	}

	public void setBugs(String bugs) {
		this.bugs = bugs;
	}

	public String getMarkdown() {
		return markdown;
	}

	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}

	public String getGalleryColor() {
		return galleryColor;
	}

	public void setGalleryColor(String galleryColor) {
		this.galleryColor = galleryColor;
	}

	public String getGalleryTheme() {
		return galleryTheme;
	}

	public void setGalleryTheme(String galleryTheme) {
		this.galleryTheme = galleryTheme;
	}

	public String getQna() {
		return qna;
	}

	public void setQna(String qna) {
		this.qna = qna;
	}

	public List<Extension> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Extension> dependencies) {
		this.dependencies = dependencies;
	}

	public List<Extension> getBundledExtensions() {
		return bundledExtensions;
	}

	public void setBundledExtensions(List<Extension> bundledExtensions) {
		this.bundledExtensions = bundledExtensions;
	}

}