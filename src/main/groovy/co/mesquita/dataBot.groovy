package co.mesquita

import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements

import static groovyx.net.http.HttpBuilder.*

class dataBot {
    static void main(String[] args) {

        def http = configure {
            request.uri = 'https://www.gov.br'
        }

        String epss = http.get(){
            request.uri.path = '/ans/pt-br'
        }

        Document doc = Jsoup.parse(epss)
        Elements links = doc.select('a[href]')

        def pattern = ~/Espa.o do Prestador de Servi.os de Sa.de/
        def prestadoresURL
        for (Element link : links) {
            if (link.text() =~ pattern) {
                prestadoresURL = link.attr("href")
                break
            }
        }

        println(prestadoresURL)
    }
}