(ns tictag-client.sound
  (:import [javax.sound.sampled.AudioSystem]))

(defn play! [resource]
  (let [input-stream (javax.sound.sampled.AudioSystem/getAudioInputStream resource)
        clip         (javax.sound.sampled.AudioSystem/getClip)]
    (try (doto clip
           (.open input-stream)
           (.start))
         (catch javax.sound.sampled.LineUnavailableException e
           nil))))

