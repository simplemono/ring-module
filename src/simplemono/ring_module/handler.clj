(ns simplemono.ring-module.handler
  "Use:

       (update system-map :ring/handlers conj #'my-ring-handler)

   to register a new Ring handler in the system. Such a Ring handler will handle
   the incoming request, if it returns a non-nil value, otherwise the next
   registered handler is asked. The Ring handler receives the `system-map` plus
   an `:ring/request` entry with the Ring request map. The Ring handler then
   needs to add the `:ring/response` to the `system-map`. By receiving the
   `system-map` the Ring handler can leverage further conventions like the one
   that you will find the Datomic database connection in the `:datomic/con`
   entry.

   Another convention is that `(:ring/handler system-map)` is the main entry
   point for all incoming HTTP requests of a system.")

(defn ring-handler
  [{:keys [ring/handlers] :as w}]
  (fn [request]
    (some
      (fn [handler]
        (:ring/response
         (handler (assoc w
                         :ring/request request))))
      handlers)))

(defn add
  [w]
  (assoc w
         :ring/handler
         (ring-handler w)))
