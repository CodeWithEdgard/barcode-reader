package com.br.service;

import java.util.Optional;
import java.util.Set;
import com.br.domain.Product;
import com.br.repository.ProductRepository;

public class ProductService {

    private ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public void salvarProduto(Product product) {

        repository.salvarProduto(product);
    }

    public Set<Product> listarTodos() {

        return repository.listarTodos();
    }

    public Optional<Product> encontrarPeloCodigo(String codigo) {

        return repository.encontrarPeloCodigo(codigo);
    }
}
