package p

import p.bar_.*

object bar_ { trait Bar }

object foo_ { trait Foo extends Bar }
export foo_.*
