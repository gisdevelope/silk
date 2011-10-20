package de.fuberlin.wiwiss.silk.plugins.transformer

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import de.fuberlin.wiwiss.silk.plugins.Plugins
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StemmerTransformerTest extends FlatSpec with ShouldMatchers {
  Plugins.register()

  val transformer = new StemmerTransformer()

  "StemmerTransformer" should "return 'abolish'" in {
    transformer.evaluate("abolished") should equal("abolish")
  }

  val transformer1 = new StemmerTransformer()

  "StemmerTransformer" should "return 'abomin'" in {
    transformer1.evaluate("abominations") should equal("abomin")
  }
}