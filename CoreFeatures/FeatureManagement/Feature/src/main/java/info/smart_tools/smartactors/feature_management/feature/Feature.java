package info.smart_tools.smartactors.feature_management.feature;

import info.smart_tools.smartactors.base.interfaces.ipath.IPath;
import info.smart_tools.smartactors.feature_management.interfaces.ifeature.IFeature;

import java.util.Set;

/**
 * Implementation of {@link IFeature}
 */
public class Feature implements IFeature {

    private String name;
    private Set<String> dependencies;
    private IPath featureLocation;
    private boolean failed;
    private String groupId;
    private String version;

    /**
     * Creates instance of {@link IFeature} by specific arguments
     * @param name the feature name
     * @param dependencies feature dependencies
     * @param location the feature location
     */
    public Feature(final String name, final Set<String> dependencies, final IPath location) {
        this.name = name;
        this.dependencies = dependencies;
        this.featureLocation = location;
        this.failed = false;
    }

    /**
     * Creates instance of {@link IFeature} by specific arguments
     * @param name the feature name
     * @param groupId the feature group ID
     * @param version the feature version
     * @param featureLocation the feature location
     */
    public Feature(final String name, final String groupId, final String version, final IPath featureLocation) {
        this.name = name;
        this.groupId = groupId;
        this.version = version;
        this.featureLocation = featureLocation;
        this.failed = false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public IPath getFeatureLocation() {
        return this.featureLocation;
    }

    @Override
    public boolean isFailed() {
        return this.failed;
    }

    @Override
    public void setFailed(final boolean failed) {
        this.failed = failed;
    }

    @Override
    public void setName(final String featureName) {
        this.name = featureName;
    }

    @Override
    public void setDependencies(final Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public void setFeatureLocation(final IPath location) {
        this.featureLocation = (IPath) location;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public void setGroupId(final String groupId) {
        this.groupId = (String) groupId;
    }

    @Override
    public void setVersion(final String version) {
        this.version = (String) version;
    }
}
