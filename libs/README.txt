BURAYA sunucunuzdaki gercek PlotSquared jar dosyasini AYNI ISIMLE koyun:

  PlotSquared-Bukkit-18.12.12-be48507-2053.jar

Ornek:
  cp /path/to/server/plugins/PlotSquared-Bukkit-18.12.12-be48507-2053.jar libs/

Sonra bu dosyayla birlikte (README.txt degil, jar dosyasiyla) git'e commit edip
push edin. Isim pom.xml icindeki <systemPath> ile birebir eslesmeli, yoksa
"Could not find artifact" hatasi alirsiniz.
