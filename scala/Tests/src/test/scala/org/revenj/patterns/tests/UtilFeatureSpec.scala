package org.revenj.patterns
package tests

import org.scalatest._, matchers._, junit._
import org.junit.runner.RunWith

import scala.Array.canBuildFrom
import postgres.Utils._

@RunWith(classOf[JUnitRunner])
class UtilFeatureSpec extends FeatureSpec with GivenWhenThen with Matchers {

  feature("buildURI"){
    scenario("Escaping is a stable function") {
      Given("a null input string")
      When("it is escaped via a character")
      Then("a null string must be returned")
      buildURI(null) should be (null)
    }

    scenario("Escaping an array contains null") {
      intercept[NullPointerException] {
        buildURI(Array(null, "f/"))
      }
    }

    scenario("Test 3 : array argument does not contain separator") {
      buildURI(Array("ab", "f")) should be ("""ab/f""")
    }

    scenario("Test 4 : array argument size = 0") {
      intercept[IllegalArgumentException] {
        buildURI(Array())
      }
    }

    scenario("Test 5 : normal array argument") {
      buildURI(Array("a/b", "f/")) should be ("""a\/b/f\/""")
    }

    scenario("Test 6 : array argument contains an empty string") {
      buildURI(Array("", "f/")) should be ("""/f\/""")
    }

    scenario("Test 7 : Joining with a regular expression special character") {
      Given("a string consisting of some special characters")
      When("it is escaped via that character")
      Then("the special characters must be duplicated")
      buildURI(Array("a/b","c/d")) should be ("""a\/b/c\/d""")
    }
  }

  feature("jvm.io.buildSimpleUriList") {
    scenario("1 : null array argument") {
      buildSimpleUriList(null) should be (null)
    }

    scenario("2 : empty array") {
      intercept[IllegalArgumentException] {
        buildSimpleUriList(Array())
      }
    }

    scenario("3 : array contains null") {
      intercept[NullPointerException] {
        buildSimpleUriList(Array("",null))
      }
    }

    scenario("4 : array contains empty string") {
        buildSimpleUriList(Array("","\\/","'")) should be ("""'','\/',''''""")
    }

    scenario("5 : simple build uri test without special chars") {
      buildSimpleUriList(Array("ab","cd")) should be ("""'ab','cd'""")
    }

    scenario("6 : simple join uri test with both special chars") {
      buildSimpleUriList(Array("a'b","c\\/d")) should be ("""'a''b','c\/d'""")
    }

    scenario("7 : test a//b , a'//b , 'ab// , //ab', // ") {
      buildSimpleUriList(Array("a\\/b","a'\\/b","'ab\\/","\\/ab'","\\/")) should be ("""'a\/b','a''\/b','''ab\/','\/ab''','\/'""")
    }
  }

  feature("jvm.io.buildCompositeUriList") {
    scenario("1 : null array argument") {
      buildCompositeUriList(null) should be (null)
    }

    scenario("2 : empty array") {
      intercept[IllegalArgumentException] {
        buildCompositeUriList(Array())
      }
    }

    scenario("3 : array contains null") {
      intercept[NullPointerException] {
        buildCompositeUriList(Array("",null))
      }
    }
    scenario("4.1 : array contains empty string") {
      buildCompositeUriList(Array("")) should be ("""('')""")
    }

    scenario("4.2 : array contains ' character") {
      buildCompositeUriList(Array("a'b")) should be ("""('a''b')""")
    }

    scenario("4.3 : array contains \\/ characters") {
      buildCompositeUriList(Array("a\\/b")) should be ("""('a/b')""")
    }

    scenario("4.4 : array contains / character") {
      buildCompositeUriList(Array("a/b")) should be ("""('a','b')""")
    }

    scenario("5 : simple test without special chars") {
      buildCompositeUriList(Array("ab","cd")) should be ("""('ab'),('cd')""")
    }

    scenario("6 : simple test with special chars") {
      buildCompositeUriList(Array("a'b","c\\/d","e/f")) should be ("""('a''b'),('c/d'),('e','f')""")
    }

    scenario("7 : test / at both beginning and end, multiple // and /'/ ") {
      buildCompositeUriList(Array("/a\\/b\\/c/","/'/")) should be ("""('','a/b/c',''),('','''','')""")
    }

    scenario("8 : more complex test (\\/\\//,/)") {
      buildCompositeUriList(Array("\\/\\//","/")) should be ("""('//',''),('','')""")
    }

    scenario("9 : \\/a\\/ and /a/ and 'a' ") {
      buildCompositeUriList(Array("\\/a\\/","/a/","'a'")) should be ("""('/a/'),('','a',''),('''a''')""")
    }

    scenario("10 : a\\\\/b/\\/c") {
      buildCompositeUriList(Array("a\\\\/b/\\/c")) should be ("""('a\','b','/c')""")
    }
  }
}
