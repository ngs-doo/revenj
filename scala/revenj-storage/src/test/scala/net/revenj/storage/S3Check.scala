package net.revenj.storage

import org.specs2.mutable.Specification

class S3Check extends Specification {
  "S3 smoke test" >> {
    S3 ne null
  }
}
