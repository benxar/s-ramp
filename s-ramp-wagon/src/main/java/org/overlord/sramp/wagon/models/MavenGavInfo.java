/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.wagon.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.wagon.resource.Resource;

/**
 * GAV info for maven.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenGavInfo {

	/**
	 * <p>Parses the resource name and returns the GAV information.  An example of
	 * a Resource that might be passed in is:</p>
	 * <br/>
	 * <b>Format 1:</b> <code>org/example/schema/my-schema/1.3/my-schema-1.3.xsd</code><br/>
	 * <b>Format 2:</b> <code>xsd/XsdDocument/29873-21983-2497822-1989/1.0/29873-21983-2497822-1989-1.0.pom</code>
	 *
	 * @param resource the Wagon {@link Resource}
	 * @return the maven GAV info
	 */
	public static MavenGavInfo fromResource(Resource resource) {
		String resourceName = resource.getName();
		List<String> segments = new ArrayList<String>(Arrays.asList(resourceName.split("/")));
		String filename = segments.remove(segments.size() - 1);
		String type = filename.substring(filename.lastIndexOf('.') + 1);
		boolean hash = false;
		if (filename.endsWith(".sha1")) {
			type = filename.substring(0, filename.length() - 5);
			type = type.substring(type.lastIndexOf('.') + 1) + ".sha1";
			hash = true;
		}
		if (filename.endsWith(".md5")) {
			type = filename.substring(0, filename.length() - 4);
			type = type.substring(type.lastIndexOf('.') + 1) + ".md5";
			hash = true;
		}
		String version = segments.remove(segments.size() - 1);
		String artifactId = segments.remove(segments.size() - 1);
		String groupId = StringUtils.join(segments, ".");
		String classifier = extractClassifier(filename, version, type);
		boolean snapshot = version.endsWith("-SNAPSHOT");
		String snapshotId = null;
		if (snapshot && !filename.contains(version)) {
			snapshotId = extractSnapshotId(filename, version, type, classifier);
		}

		MavenGavInfo gav = new MavenGavInfo();
		gav.setFullName(resourceName);
		gav.setName(filename);
		gav.setGroupId(groupId);
		gav.setArtifactId(artifactId);
		gav.setVersion(version);
		gav.setClassifier(classifier);
		gav.setType(type);
		gav.setHash(hash);
		gav.setSnapshot(snapshot);
		gav.setSnapshotId(snapshotId);
		return gav;
	}

	/**
	 * Extract the classifier information (if any) from the file name.  Examples include:
	 *
	 * <ul>
	 *   <li>commons-io-1.3.2.jar - no classifier</li>
	 *   <li>commons-io-1.3.2-tests.jar - "tests"</li>
	 *   <li>test-wagon-push-0.0.1-20120921.113704-1.pom - no classifier</li>
	 *   <li>test-wagon-push-0.0.1-20120921.113704-1-sources.jar.sha1 - "sources"</li>
	 *   <li>maven-metadata.xml - no classifier</li>
	 * </ul>
	 *
	 * @param filename
	 * @param version
	 * @param type
	 * @return the classifier
	 */
	private static String extractClassifier(String filename, String version, String type) {
		if (!filename.endsWith(type))
			return null;

		String classifier = null;
		String w = filename;
		// Strip off the file extension (type)
		w = w.substring(0, w.indexOf(type) - 1);

		if (w.contains(version)) {
			int vidx = w.indexOf(version) + version.length();
			if (vidx < w.length()) {
				// Give me everything between the version and type (exclusive).  What is left,
				// if anything, should be the classifier.
				w = w.substring(vidx);
				if (w.startsWith("-")) {
					classifier = w.substring(1);
				}
			}
		} else if (version.endsWith("-SNAPSHOT")) {
			String v2 = version.substring(0, version.indexOf("-SNAPSHOT"));
			int vidx = w.indexOf(v2) + v2.length() + 1;
			if (vidx < w.length()) {
				w = w.substring(vidx);
				String [] split = w.split("-");
				if (split.length == 3) {
					classifier = split[2];
				}
			}
		}

		return classifier;
	}

	/**
	 * Extracts the snapshot-id from the given filename.
	 * @param filename
	 * @param version
	 * @param type
	 * @param classifier
	 * @return a snapshot id or null if not found
	 */
	private static String extractSnapshotId(String filename, String version, String type, String classifier) {
		String front = version.substring(0, version.indexOf("-SNAPSHOT"));
		String back = "." + type;
		if (classifier != null) {
			back = "-" + classifier + back;
		}
		int idx1 = filename.indexOf(front) + front.length() + 1;
		int idx2 = filename.indexOf(back);

		if (idx1 > 0 && idx1 < filename.length() && idx2 > 0 && idx2 < filename.length()) {
			return filename.substring(idx1, idx2);
		} else {
			return null;
		}
	}

	private String fullName;
	private String name;
	private String groupId;
	private String artifactId;
	private String version;
	private String classifier;
	private String type;
	private boolean hash;
	private boolean snapshot;
	private String snapshotId;

	/**
	 * Default constructor.
	 */
	public MavenGavInfo() {
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getName()).append("\n");
		builder.append("  groupId: ");
		builder.append(getGroupId()).append("\n");
		builder.append("  artifactId: ");
		builder.append(getArtifactId()).append("\n");
		builder.append("  version: ");
		builder.append(getVersion()).append("\n");
		builder.append("  classifier: ");
		builder.append(getClassifier()).append("\n");
		builder.append("  type: ");
		builder.append(getType()).append("\n");
		builder.append("  isHash: ");
		builder.append(isHash()).append("\n");
		builder.append("  isSnapshot: ");
		builder.append(isSnapshot()).append("\n");
		return builder.toString();
	}

	/**
	 * @return the classifier
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier the classifier to set
	 */
	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the hash
	 */
	public boolean isHash() {
		return hash;
	}

	/**
	 * @param hash the hash to set
	 */
	public void setHash(boolean hash) {
		this.hash = hash;
	}

	/**
	 * @return the snapshot
	 */
	public boolean isSnapshot() {
		return snapshot;
	}

	/**
	 * @param snapshot the snapshot to set
	 */
	public void setSnapshot(boolean snapshot) {
		this.snapshot = snapshot;
	}

	/**
	 * @return the snapshotId
	 */
	public String getSnapshotId() {
		return snapshotId;
	}

	/**
	 * @param snapshotId the snapshotId to set
	 */
	public void setSnapshotId(String snapshotId) {
		this.snapshotId = snapshotId;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}
