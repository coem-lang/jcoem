package com.ky.coem;

import java.util.List;

interface CoemCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}
