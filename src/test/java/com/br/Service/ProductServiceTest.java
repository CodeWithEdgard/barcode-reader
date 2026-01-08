package com.br.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.br.domain.Product;
import com.br.repository.ProductRepository;
import com.br.repository.ProductRepositoryImpl;
import com.br.service.ProductService;

public class ProductServiceTest {

    private Product product;
    private ProductRepository repository;
    private ProductService service;

    @BeforeEach
    void setUp() {

        product = new Product("an1/2301000");
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
        service.salvarProduto(new Product("an4/230500"));

        assertEquals(2, service.listarTodos().size());
    }

    @Test
    void encontrarPeloCodigo() {

        service.salvarProduto(product);
        service.salvarProduto(new Product("an4/230500"));

        assertEquals(product, service.encontrarPeloCodigo(product.getCodigo()).get());
    }
}
