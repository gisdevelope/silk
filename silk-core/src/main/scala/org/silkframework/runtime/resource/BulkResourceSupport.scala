package org.silkframework.runtime.resource

import java.io.File
import java.util.logging.Logger
import BulkResource._

/**
  * Trait for Datasets that need to support zipped files with multiple resources.
  * Provides a checkResource function that returns a Resource or a BulkResource, depending on the input.
  * See @BulkResource.
  * To fully support zipped or partitioned resources the implementing class should use the methods of
  * BulkResource. It provides a input stream on the concatenated content of the zip or a set of input streams
  * that can be used in more complex methods of combining the input.
  */
trait BulkResourceSupport {

  private val log: Logger = Logger.getLogger(this.getClass.getSimpleName)

  /**
    * Returns a BulkResource depending on the given inputs location and name.
    * A BulkResource is returned if the file belonging to the given resource ends with .zip or is a
    * directory.
    *
    * @param resource WritableResource tha may be zip or folder
    * @return instance of BulkResource
    */
  def asBulkResource(resource: WritableResource): BulkResource = {
    if (resource.name.endsWith(".zip") && !new File(resource.path).isDirectory) {
      log info "Zip file Resource found."
      BulkResource(resource)
    }
    else if (new File(resource.path).isDirectory) {
      log info "Resource Folder found."
      BulkResource(resource)
    }
    else {
      throw new IllegalArgumentException(resource.path + " is not a bulk resource.")
    }
  }

  /**
    * Returns true if the given resource is a BulkResource and false otherwise.
    * A BulkResource is detected if the file belonging to the given resource ends with .zip or is a
    * directory.
    *
    * @param resource WritableResource to check
    * @return true if an archive or folder
    */
  def isBulkResource(resource: WritableResource): Boolean = {
    resource.name.endsWith(".zip") && !new File(resource.path).isDirectory
  }


}