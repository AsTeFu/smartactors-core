package info.smart_tools.smartactors.das.utilities.interfaces;

import info.smart_tools.smartactors.das.utilities.exception.InvalidCommandLineArgumentException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public interface ICommandLineArgsResolver {

    String getProjectName()
            throws InvalidCommandLineArgumentException;

    boolean isProjectName();

    String getCommand()
            throws InvalidCommandLineArgumentException;

    boolean isCommand();

    String getFeatureName()
            throws InvalidCommandLineArgumentException;

    boolean isFeatureName();

    String getActorName()
            throws InvalidCommandLineArgumentException;

    boolean isActorName();

    String getPluginName()
            throws InvalidCommandLineArgumentException;

    boolean isPluginName();

    String getGroupId()
            throws InvalidCommandLineArgumentException;

    boolean isGroupId();

    String getVersion()
            throws InvalidCommandLineArgumentException;

    boolean isVersion();

    String getUploadRepositoryId()
            throws InvalidCommandLineArgumentException;

    boolean isUploadRepositoryId();

    String getUploadRepositoryUrl()
            throws InvalidCommandLineArgumentException;

    boolean isUploadRepositoryUrl();

    String getServerDirectory()
            throws InvalidCommandLineArgumentException;

    boolean isServerName();

    String getSourceLocation()
            throws InvalidCommandLineArgumentException;

    boolean isSourceLocation();

    String getArtifactId()
            throws InvalidCommandLineArgumentException;

    boolean isArtifactId();

    String getPath()
            throws InvalidCommandLineArgumentException;

    boolean isPath();

    boolean isHelp();

    void printHelp();
}
