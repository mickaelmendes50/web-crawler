package co.mesquita

import groovyx.net.http.HttpBuilder
import groovyx.net.http.optional.Download
import org.jsoup.Jsoup
import org.jsoup.nodes.*
import org.jsoup.select.Elements

import java.util.regex.Pattern

import static groovyx.net.http.HttpBuilder.*

class dataBot {

    // Obtem o html da pagina em formato de string
    static Document getUriPath(HttpBuilder http, String URL) {
        String htmlContent = http.get(){
            request.uri.path = URL
        }
        Document doc = Jsoup.parse(htmlContent)
        return doc
    }

    // Obtem o link contido no href da tag desejada
    static String getHrefContent(Document doc, Pattern pattern, boolean split) {
        Elements links = doc.select('a[href]')
        def URL = ""
        for (Element link : links) {
            if (link.text() =~ pattern) {
                URL = link.attr("href")
                break
            }
        }

        if (!split)
            return URL

        URL = URL.split('gov.br')
        return URL[1]
    }

    static void main(String[] args) {

        // Define a URL base
        def http = configure {
            request.uri = 'https://www.gov.br'
        }

        final String ANS_URL = '/ans/pt-br'
        Document ans = getUriPath(http, ANS_URL)

        // Obtem a URL da pagina "Espaço do Prestador de Serviços de Saúde"
        def pattern = ~/Espa.o do Prestador de Servi.os de Sa.de/
        final String EPSS_URL = getHrefContent(ans, pattern, true)
        Document epss = getUriPath(http, EPSS_URL)

        // Obtem a URL da pagina "Troca de Informações na Saúde Suplementar"
        pattern = ~/TISS.*/
        final String TISS_URL = getHrefContent(epss, pattern, true)
        Document tiss = getUriPath(http, TISS_URL)

        // Obtem a URL da pagina "Padrão TISS - Versão Mês/Ano"
        pattern = ~/Clique aqui para acessar a vers.o.*/
        final String MES_TISS_URL = getHrefContent(tiss, pattern, true)
        Document mesTiss = getUriPath(http, MES_TISS_URL)

        // Obtem a URL da pagina "Padrão TISS – Histórico das versões"
        pattern = ~/Clique aqui para acessar todas as versões dos Componentes/
        final String HISTORICO_TISS_URL = getHrefContent(tiss, pattern, true)
        Document historicoTiss = getUriPath(http, HISTORICO_TISS_URL)

        // Obtem a URL da pagina "Padrão TISS – Tabelas Relacionadas"
        pattern = ~/Clique aqui para acessar as planilhas/
        final String TABELA_TISS_URL = getHrefContent(tiss, pattern, true)
        Document tabelaTiss = getUriPath(http, TABELA_TISS_URL)

        // Obtem a URL para download do arquivo "Componente de Comunicação"
        pattern = ~/Componente de Comunica..o/
        final String CC_TISS_URL = getHrefContent(mesTiss, pattern, false)

        def file = configure {
            request.uri = CC_TISS_URL
        }.get {
            Download.toFile(delegate, new File('downloads/PadraoTISSComunicacao.zip'))
        }

        // Obtem os dados das tabelas da pagina de historico
        Element table = historicoTiss.select("table").get(0)
        Elements rows = table.select("tr")

        Element row = rows[0]
        Elements indice = row.select("th")
        println indice[0].text() + " " + indice[1].text() + " " + indice[2].text()

        for (int i = 1; i < rows.size()-1; i++) {
            row = rows[i]
            if (row.text().find('jan/2016')) break

            Elements cols = row.select("td")
            for (int j = 0; j < 3; j++) {
                Element col = cols[j]
                if (col != null)
                    print col.text() + " "
            }
            println ""
        }

        // Obtem a URL para download do arquivo "Tabela de erros no envio para a ANS"
        pattern = ~/Clique aqui para baixar a tabela de erros no envio para a ANS.*/
        final String XLSX_TISS_URL = getHrefContent(tabelaTiss, pattern, false)

        file = configure {
            request.uri = XLSX_TISS_URL
        }.get {
            Download.toFile(delegate, new File('downloads/tabela-erros-envio-para-ans-padrao.xlsx'))
        }
    }
}
