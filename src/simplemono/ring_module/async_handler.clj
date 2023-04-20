(ns simplemono.ring-module.async-handler
  "Use:

      (update :ring/async-handlers conj #'ring-async-handler)

   to register an asynchronous Ring handler. Instead of the usual three
   arguments the registered async Ring handlers will receive the system-map with
   the corresponding entries `:ring/request`, `:ring/respond` and `:ring/raise`.")

(defn async-ring-handler
  "The main async Ring handler of the system, which will receive all incoming
   requests and dispatch them to the registered `:ring/async-handlers`."
  [{:keys [ring/async-handlers] :as w}]
  (fn [request respond raise]
    (some
      (fn [async-handler]
        (async-handler (assoc w
                              :ring/request request
                              :ring/respond respond
                              :ring/raise raise)))
      async-handlers
      )))

(defn to-async-ring-handler
  "Converts the `sync-ring-handler` to an async one."
  [sync-ring-handler]
  (fn [{:keys [ring/request ring/respond ring/raise]}]
    (try
      (when-let [response (sync-ring-handler request)]
        (respond response))
      (catch Throwable e
        (raise e)))))

(defn add-ring-handler
  "Adds the system's `:ring/handler` to the `:ring/async-handlers`. Thereby a
   Ring-compatible HTTP server only needs to use the `:ring/async-handler` to
   have access to all registered Ring handlers (sync and async ones)."
  [{:keys [ring/handler] :as w}]
  (if-not handler
    w
    (update w
            :ring/async-handlers
            (fn [async-handlers]
              (cons (to-async-ring-handler handler)
                    async-handlers)))))

(defn add-async-ring-handler
  "Adds the main async Ring handler to the system, which will receive all incoming
   requests."
  [w]
  (assoc w
         :ring/async-handler
         (async-ring-handler w)))

(defn add
  "Adds async Ring handler support to the system `w`."
  [w]
  (-> w
      (add-ring-handler)
      (add-async-ring-handler)))
