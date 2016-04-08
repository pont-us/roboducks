(TeX-add-style-hook "report"
 (function
  (lambda ()
    (LaTeX-add-bibliographies
     "biblio")
    (TeX-add-symbols
     '("code" 1))
    (TeX-run-style-hooks
     "graphics"
     "a4wide"
     "latex2e"
     "rep11"
     "report"
     "a4paper"
     "11pt"))))

