package info.smart_tools.smartactors.core.ipath;

/**
 *  Abstract filesystem path.
 *  Takes relative or absolute path in local filesystem.
 */
public interface IPath {

    /**
     *  Returns path represented by this class.
     *  @return path as String
     */
    String getPath();

}
