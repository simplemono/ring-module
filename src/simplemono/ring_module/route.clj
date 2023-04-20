(ns simplemono.ring-module.route
  "A mini framework that allows to register routes to handle Ring requests.

   Compojure's [clout](https://github.com/weavejester/clout) is used for
   matching the `:uri` of the `:ring/request`.

   Use:

       (assoc-in w
                 [:ring/routes [:get \"/article/:title\"]]
                 #'your-ring-handler)

   to register a route in the system. The ring-handler will receive a world map
   with the an entry `:ring/route-params` that will the route parameters which
   clout extracted from the `:uri`."
  (:require [clout.core :as clout]))

(defn dispatch
  [{:keys [ring/routes ring/request] :as w}]
  (some
    (fn [[[request-method route] ring-handler]]
      (when (= (:request-method request)
               request-method)
        (when-let [route-params (clout/route-matches
                                  route
                                  request)]
          (ring-handler (assoc w
                               :ring/route-params
                               route-params)))))
    routes))

(defn add
  [w]
  (-> w
      (update :ring/handlers
              conj
              #'dispatch)))
