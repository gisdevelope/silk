package org.silkframework.execution

import org.silkframework.config.{Task, TaskSpec}
import org.silkframework.entity.{Entity, EntitySchema}
import org.silkframework.util.Identifier

/**
  * Holds entities that are exchanged between tasks.
  */
trait EntityHolder {

  /**
    * The schema of the entities
    */
  def entitySchema: EntitySchema

  /**
    * The entities in this table.
    */
  def entities: Traversable[Entity]

  /**
    * get head Entity
    */
  def headOption: Option[Entity]

  /**
    * The task that generated this table.
    * If the entity table has been generated by a workflow this is a copy of the actual task that has been executed.
    */
  def task: Task[TaskSpec]

  /**
    * Convenience function for unwrapping the task id
    * NOTE: Caution when using with essential code, prefer taskOption.map(_.id) instead
    * @return - the task id or a unified statement for a missing task id
    */
  def taskId: Identifier = task.id

  /**
    * Convenience method to get either the task label if it exists or the task ID.
    * @return
    */
  def taskLabel: String = task.metaData.formattedLabel(taskId)
}
