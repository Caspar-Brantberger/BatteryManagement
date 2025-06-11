Välkommen till mit skolprojekt av simulering av laddning. 
Förs för att testa detta program är att ladda ner detta pytonscript till visual studio code (Du måste ladda ner pyton samt flask osv,samp ändra return typen från json.dump till jsonify).
[7os9_1748969630085_chargingwebserver-v0-8-py(1).txt](https://github.com/user-attachments/files/20673807/7os9_1748969630085_chargingwebserver-v0-8-py.1.txt)

Detta program så kan du simulera en laddning där den laddar under optimala timmar där priset är som billigast för hushållsförbrukning eller vad som är billigast elpris.
 private boolean optimizeByPriceStrategy = false;// False = Lägsta hushållsförbrukning , true = lägsta elpris då får man ändra detta till true om man vill kolla lägsta elpris istället.

Du behöver använda postman för att komma åt alla funktioner
 För att starta optimering av batteriet när det ska laddas:
 http://localhost:8080/battery/start-optimization

För att kolla status på batteriet:
 http://localhost:8080/battery/status

För att manuellt ladda batteriet
http://localhost:8080/battery/charge/start

För manuellt stanna batteriet
http://localhost:8080/battery/charge/stop

För att manuellt restarta batteriet
http://localhost:8080/battery/reset
