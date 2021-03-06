package org.silkframework.workspace.activity.dataset

import org.silkframework.dataset.DatasetSpec
import org.silkframework.dataset.DatasetSpec.GenericDatasetSpec
import org.silkframework.runtime.activity.Activity
import org.silkframework.runtime.plugin.Plugin
import org.silkframework.workspace.ProjectTask
import org.silkframework.workspace.activity.TaskActivityFactory

@Plugin(
  id = "TypesCache",
  label = "Types Cache",
  categories = Array("Dataset"),
  description = "Holds the most frequent types in a dataset."
)
class TypesCacheFactory extends TaskActivityFactory[GenericDatasetSpec, TypesCache] {

  override def autoRun: Boolean = true

  def apply(task: ProjectTask[GenericDatasetSpec]): Activity[Types] = {
    new TypesCache(task)
  }
}
