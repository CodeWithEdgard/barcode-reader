package com.br.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.br.domain.Localizacao;
import com.br.domain.Product;
import com.br.domain.enums.Almoxarifado;
import com.br.domain.enums.Estoque;
import com.br.repository.ProductRepository;
import com.br.repository.ProductRepositoryImpl;
import com.br.service.ProductService;

public class ProductServiceTest {

    private Product product;
    private ProductRepository repository;
    private ProductService service;
    private Localizacao localizacao;

    @BeforeEach
    void setUp() {

        localizacao = new Localizacao(Almoxarifado.AL01, Estoque.E1, "A", "015");

        product = new Product("an1/2301000", localizacao);
        repository = new ProductRepositoryImpl();
        service = new ProductService(repository);

    }

    @Test
    void salvarProdutoComSucesso() {

        service.salvarProduto(product);

        assertEquals(1, service.listarTodos().size());
    }

    @Test
    void listarTodos() {

        service.salvarProduto(product);
        service.salvarProduto(new Product("an4/230500", localizacao));

        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void encontrarPeloCodigo() {

        service.salvarProduto(product);
        service.salvarProduto(new Product("an4/230500", localizacao));

        assertEquals(product, service.encontrarPeloCodigo(product.getCodigo()).get());
    }
}
