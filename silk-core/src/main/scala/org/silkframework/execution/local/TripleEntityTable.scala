package org.silkframework.execution.local

import org.silkframework.config.{SilkVocab, Task, TaskSpec}
import org.silkframework.dataset.rdf._
import org.silkframework.entity._
import org.silkframework.util.Uri

/**
  * Holds RDF triples.
  */
case class TripleEntityTable(entities: Traversable[Entity], task: Task[TaskSpec]) extends LocalEntities {

  override def entitySchema: EntitySchema = TripleEntityTable.schema
}

object TripleEntityTable {
  final val schema = EntitySchema(
    typeUri = Uri(SilkVocab.TripleSchemaType),
    typedPaths = IndexedSeq(
      TypedPath(Path(SilkVocab.tripleSubject), UriValueType, isAttribute = false),
      TypedPath(Path(SilkVocab.triplePredicate), UriValueType, isAttribute = false),
      TypedPath(Path(SilkVocab.tripleObject), StringValueType, isAttribute = false),
      TypedPath(Path(SilkVocab.tripleObjectValueType), StringValueType, isAttribute = false)
    )
  )

  final val LANGUAGE_ENC_PREFIX = "lg"
  final val DATA_TYPE_ENC_PREFIX = "dt"
  final val URI_ENC_PREFIX = "ur"
  final val BLANK_NODE_ENC_PREFIX = "bn"

  def convertToEncodedType(valueType: RdfNode): (String, String) = {
    valueType match {
      case PlainLiteral(v) =>
        (v, "")
      case LanguageLiteral(v, l) =>
        (v, s"$LANGUAGE_ENC_PREFIX=$l")
      case DataTypeLiteral(v, dt) =>
        (v, s"$DATA_TYPE_ENC_PREFIX=$dt")
      case BlankNode(bn) =>
        (bn, s"$BLANK_NODE_ENC_PREFIX")
      case Resource(uri) =>
        (uri, s"$URI_ENC_PREFIX")
    }
  }

  def convertToValueType(encodedType: String): ValueType = {
    encodedType.take(2) match {
      case DATA_TYPE_ENC_PREFIX =>
        CustomValueType(encodedType.drop(3))
      case LANGUAGE_ENC_PREFIX =>
        LanguageValueType(encodedType.drop(3))
      case URI_ENC_PREFIX =>
        UriValueType
      case BLANK_NODE_ENC_PREFIX =>
        BlankNodeValueType
      case _ =>
        StringValueType
    }
  }
}
