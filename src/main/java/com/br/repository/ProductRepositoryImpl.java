package com.br.repository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import com.br.domain.Product;

public class ProductRepositoryImpl implements ProductRepository {

    private final Set<Product> bancoInMemory = new HashSet<>();

    @Override
    public void salvarProduto(Product product) {

        bancoInMemory.add(product);

    }

    @Override
    public Set<Product> listarTodos() {

        return new HashSet<>(bancoInMemory);
    }

    @Override
    public Optional<Product> encontrarPeloCodigo(String codigo) {

        return bancoInMemory.stream().filter(b -> b.getCodigo().equals(codigo)).findFirst();
    }



}
