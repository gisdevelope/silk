package org.silkframework.execution.local

import org.scalatest.{FlatSpec, MustMatchers}
import org.silkframework.plugins.dataset.InternalDatasetTrait

/**
  * Tests the internal dataset. This test is located in this module as the default is to use the RDF-based one from this module.
  */
class LocalInternalDatasetTest extends FlatSpec with MustMatchers {
  System.setProperty("dataset.internal.plugin", "inMemory")
  "LocalInternalDataset" should "store and retrieve data" in {
    val exec = LocalExecution(useLocalInternalDatasets = true)
    for(id <- Seq(None, Some("id"), Some("id2"))) {
      val ds = {
        val tempDs = exec.createInternalDataset(id)
        tempDs mustBe a[InternalDatasetTrait]
        tempDs.asInstanceOf[InternalDatasetTrait]
      }
      val sink = ds.tripleSink
      sink.init()
      sink.writeTriple("s" + id.getOrElse("None"), "b", "o")
      sink.close()
      ds.sparqlEndpoint.select("SELECT ?s WHERE {?s ?p ?o}").bindings.size mustBe 1
    }
  }
}