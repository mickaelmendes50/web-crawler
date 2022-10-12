package co.mesquita

import static groovyx.net.http.HttpBuilder.*
import org.jsoup.nodes.Document

class dataBot {


    static void main(String[] args) {

        def http = configure {
            request.uri = 'https://www.gov.br'
        }

        def epss = http.get(){
            request.uri.path = '/ans/pt-br'
        }

        def links = epss.'**'.findAll { it.name() == 'a' }*.@href*.text()

        println links
    }
}