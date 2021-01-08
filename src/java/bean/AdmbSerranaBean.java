/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.HinosDao;
import dao.SetorDao;
import entidades.Hinos;
import entidades.Setores;
import entidades.Usuarios;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import renderizacao.Renderizacao;
import renderizacao.RenderizacaoSono;
import upload.FileUploadView;
import util.ErroSistema;

/**
 *
 * @author Luciano
 */
@Named
@SessionScoped
public class AdmbSerranaBean implements Serializable {

    private Renderizacao renderizacao;
    private RenderizacaoSono renderizacaSono;
    private Setores setores;
    private SetorDao setorDao;
    private FileUploadView fileUploadView;
    private Hinos hinos;
    private HinosDao hinosDao;
    private Usuarios usuarios;

    private List<Hinos> listHinos;

    private List<Usuarios> listSetores;
    @Inject
    FileUploadView fuv;

    @PostConstruct
    public void init() {
        setores = new Setores();
        renderizacao = new Renderizacao();
        setorDao = new SetorDao();
        fileUploadView = new FileUploadView();
        hinos = new Hinos();
        hinosDao = new HinosDao();
        renderizacaSono = new RenderizacaoSono();
        usuarios = new Usuarios();
    }

    public void testeSessão() {
        HttpSession sessao = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        sessao.setAttribute("usuario_logado", setores);//Esete user é meu objeto modelo que pode ser persistido.
        Setores usuarioLogado = (Setores) sessao.getAttribute("usuario_logado");//Dai vc recupera o objeto usuario comas informação ajustadas no momento q salvou na sessao.
    }

    public void sessaoUsuario() throws ErroSistema {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (setores.getLogin() == null) {
            renderizacao.mudarParaLogarNoSite();
        } else {
            renderizacao.mudarParaGridUploadHino();
        }
        String sessao = setores.getLogin() + session.getId();
    }

    public String logar() throws ErroSistema {
        String logar = setorDao.buscarSetoresParaLogar(setores,usuarios);
        if (setores.getLogin().equals("sonoplasta") && setores.getSenhaConfirma().equals(logar)) {
            setores.setUsuario("Sonoplasta Louvando");

            return "/logado/sonoplasta.xhtml";

        }

        if (logar != null && setores.getSenhaConfirma().equals(logar)) {
            renderizacao.mudarParaGridUploadHino();
            renderizacao.mudarParaSiteLogado();
            renderizacao.mudarParaGridUsuarioLogado();
            return "/logado/index.xhtml";

        } else {

            adicionarMensagem("Usuario ou senha errada!", FacesMessage.SEVERITY_ERROR);
        }

        testeSessão();
        return null;
    }

    public String logout() throws ErroSistema {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        adicionarMensagem("Usuario ou senha errada!", FacesMessage.SEVERITY_ERROR);

        renderizacao.mudarParaLogarNoSite();
        setores = new Setores();

        return "/login.xhtml";
    }

    //------------------------------navegação--------------------------------
    public void exibirTelaLogin() {
        hinos = new Hinos();
        setores = new Setores();
        renderizacao.mudarParaLogarNoSite();
        renderizacao.ocultarGridUploadHino();

    }

    public void exibirTelaOuvirHinos() throws ErroSistema {
        hinos = new Hinos();
        buscarHinosPeloSetor();
        renderizacao.mudarParaDataTableHinos();

    }

    public void exibirBuscarHinosPeloSetor() throws ErroSistema {
        setores.setUsuario("Sonoplasta Louvando");
        hinos = new Hinos();
        renderizacaSono.mudarParaPesquisarSetor();

    }

    public void exibirTelaCadastrar() {
        renderizacao.mudarParaCadastrarNoSite();
    }

    public void exibirTelaEnviarHino() {
        renderizacao.mudarParaGridUploadHino();
        hinos = new Hinos();

    }
    //-------------------------Logar--------------------------

    public void salvarSetor() throws ErroSistema {
        if (!setores.getSetor().equals("") && !setores.getLogin().equals("")) {
            if (setores.getSenha().equals(setores.getSenhaConfirma()) && !setores.getSenha().equals("") && !setores.getSenhaConfirma().equals("")) {
                setorDao.salvarSetor(hinos, fileUploadView, setores);
                setores = new Setores();
                renderizacao.mudarParaLogarNoSite();
                adicionarMensagem("Salvo com sucesso!", FacesMessage.SEVERITY_INFO);
            } else {
                adicionarMensagem("Senhas diferentes!", FacesMessage.SEVERITY_ERROR);
                adicionarMensagem("Dados não insiridos!", FacesMessage.SEVERITY_ERROR);
            }

        } else {
            adicionarMensagem("Insira dados em todos campos!", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void logars() throws ErroSistema {
        String logar = setorDao.buscarSetoresParaLogar(setores,usuarios);
        if (logar != null && setores.getSenhaConfirma().equals(logar)) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            renderizacao.mudarBuscarSetorParaUpload();
        } else {
            adicionarMensagem("Senha ou Login incorreto!", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void buscarSetorParaUploadHino() throws ErroSistema {
        if (fileUploadView.getNomeArquivoReal() == null) {
            setorDao.buscarSetoresPeloId(setores);
            renderizacao.mudarParaGridUploadHino();
        } else {
            fileUploadView.deletaArquivo();
            fileUploadView.setExibirPlayer(false);
        }

    }

    public List<Hinos> buscarHinosAutoComplete(String query) throws ErroSistema {
        String queryLowerCase = query.toLowerCase();
        listHinos = hinosDao.buscarLouvoresPeloIdSetor(hinos, setores, usuarios);

        return listHinos.stream().filter(t -> Integer.toString(t.getNumeroHino()).toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public List<Usuarios> buscarSetoresAutoComplete(String query) throws ErroSistema {
        String queryLowerCase = query.toLowerCase();
        listSetores = setorDao.buscarSetores(usuarios);

        return listSetores.stream().filter(t -> t.getSetor().toLowerCase().startsWith(queryLowerCase)).collect(Collectors.toList());
    }

    public void cancelarUploadDeHino() {
        fuv.ocultarPlayerDOHino();
        fuv.deletaArquivo();
        hinos = new Hinos();
        adicionarMensagem("Cancelado com sucesso!", FacesMessage.SEVERITY_INFO);

    }

    // ---------------------hinos-----------------------
    public void salvarHinos() throws ErroSistema {
        hinosDao.salvarHino(hinos, fileUploadView, setores);
        fuv.setExibirPlayer(false);
        hinos = new Hinos();
        fuv.setNomeArquivoReal(null);
        adicionarMensagem("Upload Realizado!", FacesMessage.SEVERITY_INFO);

    }

    public void buscarHinosPeloSetor() throws ErroSistema {
        listHinos = hinosDao.buscarLouvoresPeloIdSetor(hinos, setores, usuarios);
        if (listHinos.isEmpty() || listHinos == null) {
            adicionarMensagem("Hinos não encontrado!", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void buscarHinosPeloSetorSonoplasta() throws ErroSistema {
        System.out.println("Setor atual " + setores.getLogin());
        listHinos = hinosDao.buscarLouvoresPeloIdSetorSonoplasta(hinos, setores, usuarios);
        setores.setUsuario("Sonoplasta Louvando");
        renderizacaSono.mudarParaPesquisarHino();

        if (listHinos.isEmpty() || listHinos == null) {
            adicionarMensagem("Hinos não encontrado!", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void buscarHinosPeloIdHino() throws ErroSistema {

        listHinos = hinosDao.buscarLouvoresPeloIdDoHino(hinos, setores);
        if (listHinos.isEmpty() || listHinos == null) {
            adicionarMensagem("Hinos não encontrado!", FacesMessage.SEVERITY_ERROR);
        }
    }

    public void exibirHinoSelecionado(Hinos hinos) {
        renderizacao.mudarParaHinoSelecionado();
        this.hinos = hinos;

    }

    public void exibirHinoSelecionadoSonoplasta(Hinos hinos) {
        this.hinos = hinos;
        setores.setUsuario("Sonoplasta Louvando");
        renderizacaSono.mudarPlayerHino();

    }

    //----------------------hinos letras----------------
    public void buscarLetraHino() {
    }

    public void adicionarMensagem(String mensagem, FacesMessage.Severity tipoErro) {
        FacesMessage fm = new FacesMessage(tipoErro, mensagem, null);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public Renderizacao getRenderizacao() {
        return renderizacao;
    }

    public Setores getSetores() {
        return setores;
    }

    public void setSetores(Setores setores) {
        this.setores = setores;
    }

    public SetorDao getSetorDao() {
        return setorDao;
    }

    public void setSetorDao(SetorDao setorDao) {
        this.setorDao = setorDao;
    }

    public FileUploadView getFileUploadView() {
        return fileUploadView;
    }

    public void setFileUploadView(FileUploadView fileUploadView) {
        this.fileUploadView = fileUploadView;
    }

    public List<Usuarios> getListSetores() {
        return listSetores;
    }

    public void setListSetores(List<Usuarios> listSetores) {
        this.listSetores = listSetores;
    }

    

    public Hinos getHinos() {
        return hinos;
    }

    public void setHinos(Hinos hinos) {
        this.hinos = hinos;
    }

    public HinosDao getHinosDao() {
        return hinosDao;
    }

    public void setHinosDao(HinosDao hinosDao) {
        this.hinosDao = hinosDao;
    }

   

    public FileUploadView getFuv() {
        return fuv;
    }

    public void setFuv(FileUploadView fuv) {
        this.fuv = fuv;
    }

    public List<Hinos> getListHinos() {
        return listHinos;
    }

    public void setListHinos(List<Hinos> listHinos) {
        this.listHinos = listHinos;
    }

    public RenderizacaoSono getRenderizacaSono() {
        return renderizacaSono;
    }

    public Usuarios getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Usuarios usuarios) {
        this.usuarios = usuarios;
    }

      

}
