package com.xing.mars.operation

import org.scalatest.{Matchers, Outcome, fixture}

/**
  * @author Mikhail Nemenko { @literal <nemenkoma@gmail.com>}
  */
class FileJournalSpec extends fixture.FlatSpec with Matchers {

  override protected def withFixture(test: OneArgTest): Outcome = {
    val fixture = FixtureParam("")
    this.withFixture(test.toNoArgTest(fixture))
  }

  final case class FixtureParam(x: String)
}
