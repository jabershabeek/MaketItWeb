MaketItWeb
==========

Briefly stated, MaketItWeb is a web server that allows you to run any swing application inside your web browser using only pure HTML5.

The best days of swing framework are gone. Desktop applications lost popularity and everything is forced to be online and mobile. But what about existing application? Using applet technology proved to be unsecure, rewriting the application to web technology is too expesive. This is where MaketItWeb can help you. With MaketItWeb, your application is securely running on server and user's browser only displays the application window. All this without changing single line of source code.

How to use MakeItWeb?
====================

Easiest way to try MakeItWeb on your computer is that you only need to download the latest distribution, unzip it, where you can find the config.prperties file, by Default which will contain

port=8286 host=jaffer swingmainclass=com.sun.swingset3.SwingSet3,rwdtester.Starter

where you can change the port, host, swing applications main class. You can have more than one swing applications, but the main class names should be separated by (,). once you configured the property files. start the start.bat file to start the server. once the server is started successfully, open your browser and type the following url, http://[host]:[port]/?app_Id=[index of the application to open] example you configured the config.propertise file as.

port=8286 host=jaffer swingmainclass=com.sun.swingset3.SwingSet3,rwdtester.Starter

the to open the first application hit, http://jaffer:8286/?app_Id=0 - this will open the application with com.sun.swingset3.SwingSet3 as the main class

Make sure that the swing application window is not minimized, because minimizing the windown causes incorrect rendering. If you want to try your own swing application, you need to adjust the config.properties file in the extarcted folder.
You need to place your application and its required libs to swinglib directory in flat structure (no sub-directories).
