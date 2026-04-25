# fmjrey/invoke

    fmjrey/invoke {:git/tag "TAG" :git/sha "SHA"}

Utility library to invoke a clojure function in an external process via the
clojure CLI and capture its result as clojure data.

This project is essentially a copy of the clojure namespace
[`clojure.tools.deps.interop`](https://clojuredocs.org/clojure.tools.deps.interop),
which is the client implementation of the
[protocol](https://clojure.org/reference/clojure_cli#function_protocol)
to programmatically invoke a clojure function in an external process using a
tool alias or name as a stepping stone for a classpath that differs from the
calling project. Any function in the classpath can be called, bearing in mind
the external process is started by the clojure CLI with its `-T` option.

This library extends that capability to any function that can be reached with
the `-X` option in any project, as opposed to only within the calling project
and tools it can reach via its aliases and runtime basis.
This is mainly useful when you need the called function to execute with a
runtime basis, working directory, and therefore `deps.edn`, that are different
from those of the calling project. Without this requirement the original
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool)
function should be sufficient, though it may require a specific `deps.edn` alias
with a dependency to the library containing the called function.

## Default clojure implementation

The [protocol](https://clojure.org/reference/clojure_cli#function_protocol)
clojure implements for calling functions via a clojure CLI external process
is a new feature released with
[clojure 1.12](https://clojure.org/news/2024/09/05/clojure-1-12-0#tool_functions)).
The API is a single function named
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool).

On the calling side it unwraps the envelope created by the callee side.
The source code for these 2 sides can be found as follows:

- calling side: [`clojure.tools.deps/invoke-tool`](https://github.com/clojure/clojure/blob/clojure-1.12.4/src/clj/clojure/tools/deps/interop.clj#L41)
- callee side: within the `exec.jar` installed by the clojure CLI, for which a
  version of the code for homebrew installations can be found
  [here](https://github.com/clojure/brew-install/blob/1.12.4/src/main/clojure/clojure/run/exec.clj#L52).

## Changes to the clojure implementation

This library does not change the clojure implementation but duplicates it.
Initial testing showed that only the calling side needs to change, as the clojure
CLI does not prevent the `-X` option from using the protocol. Therefore only the
[`clojure.tools.deps.interop`](https://clojuredocs.org/clojure.tools.deps.interop)
namespace is duplicated into `fmjrey.invoke`.

To change the runtime basis to any project it is sufficient to change the working
directory of the external process, which is made possible by the `:dir` option of
[`clojure.java.process/start`](https://clojuredocs.org/clojure.java.process/start).

## Usage

Add the following require entry:

```clojure
[fmjrey.invoke :as ext]
```

This library proposes a single `invoke` function that works the same way as
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool)
from clojure, expanding its capabilities with two additional option keys:

    :alias - Alias to invoke with -X (keyword)
    :dir - working directory for the new process (default=\".\")

As a result it's now one of `:alias,` `:tool-alias` or `:tool-name`, that must
be provided, and only the first makes use of the clojure CLI `-X` option while
the two others use `-T`.

## Acknowledgments

The additions this library provides are minor modifications.
All credits go to Rich Hickey, creator of clojure, and the clojure core team.

## License

Copyright © 2026 Rich Hickey and François Rey

Distributed under the [Eclipse Public License 1.0](http://www.eclipse.org/legal/epl-v10.html)
