(ns slides
  (:require
    [humble-deck.common :as common]
    [humble-deck.templates :as t]))

(def slides
  [[(t/image "title.webp")]
   (t/list "Problem:"
     "We love REPL"
     "Code changes during development"
     "Runtime state != file state"
     "How to bring changes to running app?")
   (t/list "Solution:"
     "Eval!")
   [(t/label "Problem: data has dependencies")]
   (t/code-list
     "(def a
  1)"

     "(def b
  (+ 10 a))"

     ["b" "b ;=> 11"]

     "(def a
  -1)"

     ["b" "b ;=> 11"])
   
   [(t/section "How to reload code?")]
   
   (t/list {:bullet nil} "Eval buffer"
     "👍 Works for single file"
     "👍 Surprisingly well"
     "👎 Multiple files"
     "👎 Dead code though")
   
   [(t/code "(defn a [x])

(defn b [y]
  (a y))")
    (t/code "; (defn a [x])

(defn b [y]
  (a y))")]

   (t/list {:bullet nil} "(require ... :reload-all)"
     "👍 Tracks dependencies"
     "👎 Eager"
     "👎 Does not unload"
     "👎 “Wrong direction”")
   
   (t/code-list
     "(ns a)"
     
     "(ns b
  (:require a))"
     
     "(ns c
  (:require b))"
     
     ["(require 'b :reload-all)" 
      "(require 'b :reload-all) ;=>"
      "(require 'b :reload-all) ;=> +a"
      "(require 'b :reload-all) ;=> +a +b"])
   
   (t/list "So what do we want?"
     "Tracks dependencies"
     "Only reload what’ve changed"
     "Reload downstream dependencies"
     "Unload before reloading")
   
   [(t/section "tools.namespace")]
   
   (t/code-list {:from 2}
     "(ns a)"
     
     "(ns b
  (:require a))"
     
     "(ns c
  (:require b))"
     
     "; touch b.clj"
     
     ["(clojure.tools.namespace.repl/refresh)" 
      "(clojure.tools.namespace.repl/refresh) ;=>"
      "(clojure.tools.namespace.repl/refresh) ;=> -c"
      "(clojure.tools.namespace.repl/refresh) ;=> -c -b"
      "(clojure.tools.namespace.repl/refresh) ;=> -c -b +b"
      "(clojure.tools.namespace.repl/refresh) ;=> -c -b +b +c"])
   
   (t/list {:bullet nil} "tools.namespace"
     "👍 Tracks dependencies"
     "👍 Only reloads what’ve changed"
     "👍 Reloads downstream dependencies"
     "👍 Unloads before reloading"
     "but..."
     "👎 First reload reloads everything"
     "👎 Reloads everything"
     "👎 RIP defonce"
     "👎 Custom reader tags"
     "👎 No (load) support"
     "👎 No (require)/(use) support"
     "👎 No cool logo")
   
   [(t/label "Meet...")]
   [(t/image "meet_clj_reload.webp")]
   
   (t/list {:bullet nil :from 4} "clj-reload"
     "👍 Tracks dependencies"
     "👍 Only reloads what’ve changed"
     "👍 Reloads downstream dependencies"
     "👍 Unloads before reloading"
     "and..."
     "👍 First reload reloads changed"
     "👍 Doesn’t reload everything"
     "👍 Supports defonce"
     "👍 Reader tags"
     "👍 Supports (load)"
     "👍 Supports (require)/(use)"
     "👍 Amazing logo")

   (t/list {:bullet nil :from 12} "tools.namespace"
     "👍 Tracks dependencies"
     "👍 Only reloads what’ve changed"
     "👍 Reloads downstream dependencies"
     "👍 Unloads before reloading"
     "but..."
     "👎 First reload reloads everything"
     "👎 Reloads everything"
     "👎 RIP defonce"
     "👎 Custom reader tags"
     "👎 No (load) support"
     "👎 No (require)/(use) support"
     "👎 No cool logo")
   
   [(t/section "DEMO tiem")]
   
   [(t/section "Problem: keep state, reload code")]

   [(t/label "Excluding namespaces")]
   (t/code-list
     "; tools.namespace"
     "(ns state
  (:require
    [clojure.tools.namespace.repl :as tns]))

(tns/disable-reload!)"
     
     "; tools.namespace, improved"
     "(ns ^{:clojure.tools.namespace.repl/load false}
  state)"
     
     "; clj-reload"
     "(clj-reload.core/init
  {:no-reload '#{state}})")

   [(t/label "Excluding vars")]
   
   (t/code-list
     "; tools.namespace

(ns ^{:clojure.tools.namespace.repl/load false}
  state)"
      
     "(def *atom
  (atom 0))"
     "; whole namespace!"
     "; have to adapt your code")
   
   (t/code-list
     "; clj-reload

(ns core)"
     
     "(def x ...)"
     "(def y ...)"
     
     "(defonce *atom
  (atom 0))"
     
     "(def uuid
  (random-uuid))")

   (t/list {:bullet nil} "defonce"
     "👍 Can mix with other code"
     "👍 Doesn’t dictate ns structure"
     "👍 Description matches behavior"
     "but we didn’t stop there...")
   
   (t/code-list
     ["(def *atom
  (atom 0))"
     
      "^:clj-reload/keep
(def *atom
  (atom 0))"]
     "^:clj-reload/keep
(defprotocol IProto
  ...)"
     "^:clj-reload/keep
(defrecord RecordKeep [t])"
     "^:clj-reload/keep
(deftype+ CustomTypeKeep [t])")

   (t/list "What about stateful resources?"
     "HTTP server"
     "Timer"
     "Thread pool"
     "DB connection"
     "Alessandra Sierra’s component"
     "tolitius/mount"
     "...")
   
   (t/list {:bullet nil} "tools.namespace"
     "1. Stop all"
     "2. Reload"
     "3. Start all"
     "👎 Not our bro")
   
   (t/list {:bullet nil} "clj-reload"
     "Unload hooks!")

   (t/code-list
     "(ns state)"

     "(mount/defstate server
  :start
  (http/run-server ... ...)

  :stop
  (http/server-stop! server))"

     "(defn before-ns-unload []
  (mount/stop #'server))")
   
   (t/list {:bullet nil :from 1} "clj-reload"
     "Unload hooks!"
     "👍 Only reload what’s needed"
     "👍 In context of actual namespace")
   
   [(t/section "How does it work?")]
   
   (t/list {:bullet nil :from 1} "How does it work?"
     "1. Find all Clojure files"
     "2. Read & parse"
     "3. Build dependency graph"
     "4. Figure out what changed"
     "5. Topological sort"
     "6. Unload"
     "7. Load")
   
   [(t/section "Reading Clojure gotchas")]
   
   (t/code-list
     "#?(:clj 1 :cljs 2)"
     "{:a 1 #?@(:clj [:b 2] :cljs [:c 3])}"
     "#user.Y {:a 1}"
     "#x 1"
     "::kw"
     "::abc/kw"
     "java.io.File"
     "File")
   
   [(t/section "How defonce works")]
   
   (t/list {:bullet nil :from 7} "How does it work?"
     "1. Find all Clojure files"
     "2. Read & parse"
     "3. Build dependency graph"
     "4. Figure out what changed"
     "5. Topological sort"
     "6. Unload"
     "7. Load")
   
   (t/list {:bullet nil :from 9} "How does it work?"
     "1. Find all Clojure files"
     "2. Read & parse"
     "3. Build dependency graph"
     "4. Figure out what changed"
     "5. Topological sort"
     "5a. Stash keeps (defonce, ...)"
     "6. Unload"
     "6a. Read & patch files"
     "7. Load")
   
   (t/code-list
     ["  ...
      
  (defonce uuid
    (random-uuid))

  ..."
   
      "  ...
      
− (defonce uuid
−   (random-uuid))
+ (def uuid
+   @#'clj-reload.stash/uuid)

  ..."])
   
   [(t/label "Protocols are even weirder...")]
  
   (t/list "Extras"
     "Reload by regexp")
   [(t/code
      "(clj-reload.core/reload
  #\"humble-deck\\..*-test\")")]
   
   (t/list {:from 1} "Extras"
     ["Reload by regexp" "(Re)load by regexp"]
     "Find namespaces"
     "Return value"
     "CIDER integration")
   
   [(t/svg "links.svg")]
   [(t/svg "questions.svg")]
   
   [(t/section "Thank you!")]])

(reset! common/*slides
  slides)

(swap! common/*slider
  assoc :max (dec (count slides)))

(swap! common/*state
  update :epoch inc)
