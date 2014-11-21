# gdx-loopy

A simple, minimal and easy game loop for lib-gdx to get you started with
interactive game development.

## Usage
Include the lib in your project.clj - snapshot only for now.

```clojure
[gdx-loopy "0.1.0-SNAPSHOT"]
```

```clojure
(require '[gdx-loopy.core :refer :all])
```

If you'd just like to get rolling use the `loop!` fn.

```clojure
;;your render function should take no arguments.
(defn render-fn
 []
 (do-something-in-here))

;;you can start the game with some defaults
(loop! render-fn)

;;or specify some configuration
(loop! render-fn {:width 800, :height 600})
```

Once your loop is up and running you can dispatch some actions to the render thread. In libgdx many operations (particularily things like loading textures) must happen on the thread with open-gl context. To dispatch to this thread use the `on-render-thread` macro or the `on-render-thread-call` fn (both return promises).

```clojure
;;on-render-thread returns a promise.
@(on-render-thread (+ 1 2))
;; => 3

;;for example creating a sprite batch must be performed on the render thread
@(on-render-thread 
  (println "creating sprite batch")
  (SpriteBatch.))
```

Have fun!

## License

Copyright © 2014 Dan Stone

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
