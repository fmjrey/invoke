# fmjrey/invoke

    fmjrey/invoke {:git/tag "TAG" :git/sha "SHA"}

Utility library to invoke a clojure function in an external process via the
clojure CLI and capture its result as clojure data. The called function must
take a single map argument like [tools.build](https://clojure.org/guides/tools_build)
task functions.

This project is essentially an augmented copy of the clojure namespace
[`clojure.tools.deps.interop`](https://clojuredocs.org/clojure.tools.deps.interop),
which is the client implementation of the
[protocol](https://clojure.org/reference/clojure_cli#function_protocol)
to execute a clojure function in an external process, using a tool alias or name
as a stepping stone for a classpath that differs from the calling project.
Any function in the classpath can be called, bearing in mind the external process
is started by the clojure CLI with its `-T` option.
This library extends that capability to any function that can be reached with
the `-X` option in any project or (uber)jar, as opposed to only within the
calling project and the tools it can reach via its aliases and runtime basis.

This is mainly useful when you need the called function to execute with a
runtime basis, working directory, and therefore `deps.edn`, that are different
from those of the calling project. Without such requirement, the original
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool)
function should be preferred, though it may require a specific `deps.edn` alias
with a dependency to the library containing the called function.

## Original clojure implementation

The [protocol](https://clojure.org/reference/clojure_cli#function_protocol)
for calling a function external process is a new feature released with
[clojure 1.12](https://clojure.org/news/2024/09/05/clojure-1-12-0#tool_functions).
The API is a single function named
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool).
On the calling side it unwraps the envelope created by the callee side.

The source code for these 2 sides can be found in the namespace
[`clojure.tools.deps.interop`](https://github.com/clojure/clojure/blob/master/src/clj/clojure/tools/deps/interop.clj)
for the calling side (client) and
[`clojure.run.exec`](https://github.com/clojure/brew-install/blob/1.12.5/src/main/clojure/clojure/run/exec.clj)
for the callee side (server). While the first is part of the clojure runtime,
the second isn't and is within `exec.jar` that is part of the clojure CLI.

## Usage

Use the following require entry:

    [fmjrey.invoke :as ext]

Then call the `invoke` function:

    fmjrey.invoke> (ext/invoke {:tool-alias :deps
                                :fn 'clojure.core/identity
                                :args {:hi :there}})
    {:hi :there}
    fmjrey.invoke> 

Keep in mind the function to be called must take a single map argument.
The `invoke` function works the same way as
[`invoke-tool`](https://clojuredocs.org/clojure.tools.deps.interop/invoke-tool)
from clojure, expanding its capabilities with 3 additional option keys:

    :alias - alias to invoke with -X (keyword)
    :dir - working directory for the new process (default=\".\")
    :cp - classpath to pass with -Scp (string)

As a result it's now one of `:cp`, `:alias,` `:tool-alias` or `:tool-name`,
that must be provided, with the last 3 being mutually exclusive. Only the
first two make use of the clojure CLI `-X` option while the last two use `-T`.
Note that `:cp` uses `-X` by default unless overridden by one of the others.

The `:cp` option value is passed to the clojure CLI with `-Scp`, which makes it
possible to invoke a function in a (uber)jar. While the
[documentation for `-Scp`](https://clojure.org/reference/clojure_cli#opt_scp)
says it prevents deps classpath computation and replaces it with the provided
classpath, experimentation has shown that if an alias is also provided, it is
still parsed and used for setting `:ns-default` which may be useful in certain
cases.
It is not known whether this is expected behavior or undetermined behavior.
In any case this library does not prevent the use of `:alias`, `:tool-alias`,
or `:tool-name` along with `:cp`, most importantly because without `-X` or `-T`
the `exec.jar` serving the invoke protocol isn't used. There are however test
cases that will fail if this behavior ever change.

## Development

To run the project's tests:

    $ clojure -M:test

What may be useful during development is how to diagnose issues and invoke
the protocol logic from the command line. To that end the `:debug` option
prints out the exact command and arguments that are executed:

    fmjrey.invoke> (invoke {:alias :cli
                            :dir "test-project"
                            :debug true
                            :fn 'test.project/return
                            :args {:hi :there}})
    args [test.project/return {:hi :there, :clojure.exec/invoke :fn}]
    Invoking:  {:dir test-project} clojure -X:cli -
    In dir:  test-project
    {:hi :there}
    fmjrey.invoke> 

The above output shows the parameters given to `clojure.java.process/start` and
the arguments given via `stdin`. To reproduce the same invocation on the CLI:

    fmjrey@computer:~/Dev/Clojure/fmjrey/invoke$ cd test-project/
    fmjrey@computer:~/Dev/Clojure/fmjrey/invoke/test-project$ clojure -X:cli -
    test.project/return {:hi :there, :clojure.exec/invoke :fn} ;; hit Ctrl-D
    {:tag :ret, :val "{:hi :there}", :ms 1}
    fmjrey@computer:~/Dev/Clojure/fmjrey/invoke/test-project$ 

We see the envelope returned with the result captured as an EDN string.

## Changes to the clojure implementation

This library does not change the clojure implementation but extends it.
Initial testing showed that only the calling side needs to change, as the clojure
CLI does not prevent the `-X` option from using the protocol. Therefore only the
[`clojure.tools.deps.interop`](https://clojuredocs.org/clojure.tools.deps.interop)
namespace is duplicated into `fmjrey.invoke`.

To change the runtime basis to any project it is sufficient to set the working
directory of the external process, which is be done with the new `:dir` option of
[`clojure.java.process/start`](https://clojuredocs.org/clojure.java.process/start)
used internally. This `:dir` option has been added to the `invoke` function,
along with `:alias` and `:cp` to support respectively invocation with `-X` and
with an additional `-Scp` parameter.

## Acknowledgments

The additions this library provides are minor modifications.
All credits go to Rich Hickey, creator of clojure, and the clojure core team.

## License

Copyright © 2026 Rich Hickey and François Rey

Distributed under the [Eclipse Public License 1.0](http://www.eclipse.org/legal/epl-v10.html)
