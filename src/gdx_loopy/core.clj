(ns gdx-loopy.core
  "A simple game loop for libgdx"
  (:import (com.badlogic.gdx ApplicationListener)
           (com.badlogic.gdx.backends.lwjgl LwjglApplication LwjglApplicationConfiguration)))

(defonce ^{:doc "As libgdx applications are global. We may as well have some state to hold the current loop
                 and provide you a reference to the running application."}
         global-loop (ref {:started? false
                           :tasks []}))

(defn listener
  "Takes a render function `frame-fn` and optionally an `on-resize` function
   and returns an ApplicationListener that can be used to start a new game loop.

   Exceptions thrown in the render thread will be caught and printed. Make sure to implement
   your own error handling."
  ([frame-fn]
   (listener frame-fn nil))
  ([frame-fn on-resize-fn]
   (proxy
     [ApplicationListener]
     []
     (pause [])
     (resume [])
     (dispose [])
     (create [])
     (render []
       (try
         (frame-fn)
         (catch Throwable e
           (println e))))
     (resize [x y]
       (when on-resize-fn
         (try
           (on-resize-fn x y)
           (catch Throwable e
             (println e))))))))

(defn map->lwjgl-configuration
  [m]
  (let [cfg (LwjglApplicationConfiguration.)]
    (set! (. cfg width) (get m :width 1024))
    (set! (. cfg height) (get m :height 768))
    (set! (. cfg fullscreen) (get m :fullscreen? false))
    (set! (. cfg title) (get m :title "Untitled"))
    (set! (. cfg vSyncEnabled) (get m :vsync? false))
    (set! (. cfg foregroundFPS) (get m :max-fps 60))
    (set! (. cfg backgroundFPS) (get m :max-fps 60))
    (set! (. cfg resizable) (get m :resizable? false))
    cfg))

(defn listener-loop!
  "Takes a listener and starts a game loop on a new thread.
   You can pass config as a map.

   The keys supported currently by config and their defaults are:
   :width 1024,
   :height 768,
   :fullscreen? false
   :title \"Untitled\"
   :vsync? false
   :max-fps 60
   :resizable? false

   Unfortunately libgdx will only allow this fn to run once for the entire application process."
  ([listener]
   (listener-loop! listener nil))
  ([listener config]
   (dosync
     (ref-set global-loop {:started? true}))
   (let [cfg (map->lwjgl-configuration config)]
     (future
       (let [app (LwjglApplication. ^ApplicationListener listener cfg)]
         (dosync
           (alter global-loop assoc :application app)))))))

(defn- take-tasks
  []
  (dosync
    (let [t (:tasks @global-loop)]
      (when-not (empty? t)
        (alter global-loop dissoc :tasks))
      t)))

(defn loop!
  "Takes a fn to call every frame and starts a game loop on a new thread.
   You can pass config as a map.

   The fn will be called every frame with NO arguments.

   The keys supported currently by config and their defaults are:
   :width 1024,
   :height 768,
   :fullscreen? false
   :title \"Untitled\"
   :vsync? false
   :max-fps 60
   :resizable? false
   :on-resize-fn nil

   Unfortunately libgdx will only allow this fn to run once for the entire application process."
  ([f]
   (loop! f nil))
  ([f config]
   (listener-loop! (listener (fn
                               []
                               (doseq [t (take-tasks)]
                                 (t))
                               (f))
                             (:on-resize-fn config)) config)))

(defn on-render-thread-call
  "Runs the fn `f` on the render thread. Returns a promise that will yield
   the result of the function call once it has been completed.

   If there is no loop taking tasks - an exception will be thrown"
  [f]
  (if (:started? @global-loop)
    (let [p (promise)
          nf (fn [] (deliver p (f)))]
      (dosync
        (alter global-loop #(assoc % :tasks (conj (:tasks % []) nf))))
      p)
    (throw (Exception. "No game loop running - cannot dispatch to the render thread."))))

(defmacro on-render-thread
  "Runs the forms on the render thread. Returns a promise that will yield
   the result of the last form.

   If there is no loop taking tasks - an exception will be thrown."
  [& forms]
  `(on-render-thread-call (fn [] ~@forms)))