package com.GodOfGames.Inventario;

import com.GodOfGames.Inventario.model.Producto;
import com.GodOfGames.Inventario.repositories.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner cargarDatos(ProductoRepository productoRepository) {
        return args -> {
            if (productoRepository.count() == 0) {
                productoRepository.saveAll(List.of(

                    // RESIDENT EVIL
                    Producto.builder().nombre("Resident Evil 0").descripcion("El origen de la pesadilla en la mansión Spencer.").precio(29.99).imagen("Resident_evil_0.png").stock(50).build(),
                    Producto.builder().nombre("Resident Evil 3 Remake").descripcion("Jill Valentine huye del imparable Nemesis.").precio(39.99).imagen("resident_evil_3.jpeg").stock(55).build(),
                    Producto.builder().nombre("Resident Evil 4 Remake").descripcion("Leon S. Kennedy en una misión de rescate en Europa.").precio(49.99).imagen("resident_evil_4.jpeg").stock(70).build(),
                    Producto.builder().nombre("Resident Evil Village").descripcion("Ethan Winters en un misterioso pueblo europeo.").precio(49.99).imagen("resident_evil_village.jpeg").stock(65).build(),
                    Producto.builder().nombre("Resident Evil Requiem").descripcion("El capítulo final de la saga Resident Evil.").precio(59.99).imagen("requiem.png").stock(80).build(),

                    // THE LAST OF US
                    Producto.builder().nombre("The Last of Us Part I").descripcion("Joel y Ellie atraviesan un mundo post-apocalíptico.").precio(49.99).imagen("the_last_of_us_part_1.jpg").stock(70).build(),
                   
                    // GOD OF WAR
                    Producto.builder().nombre("God of War (2018)").descripcion("Kratos y Atreus en la mitología nórdica.").precio(39.99).imagen("god_of_war_2018.png").stock(75).build(),
                    Producto.builder().nombre("God of War Ragnarok").descripcion("El Ragnarok se acerca y Kratos debe enfrentarlo.").imagen("ragnarok.jpg").precio(59.99).stock(80).build(),

                    // GHOST OF TSUSHIMA
                    Producto.builder().nombre("Ghost of Tsushima").descripcion("Jin Sakai defiende Tsushima de la invasión mongola.").precio(49.99).imagen("ghost_of_tsushima.jpeg").stock(60).build(),
                
                    // NEED FOR SPEED
                    Producto.builder().nombre("Need for Speed Most Wanted").descripcion("Conviértete en el corredor más buscado de Rockport.").imagen("need_for_speed.png").precio(29.99).stock(50).build(),
                
                    // GTA
                    Producto.builder().nombre("Grand Theft Auto IV").descripcion("Niko Bellic busca el sueño americano en Liberty City.").imagen("gta_iv.jpeg").precio(29.99).stock(55).build(),
                    Producto.builder().nombre("Grand Theft Auto V").descripcion("Tres criminales en los Ángeles en el mayor golpe de sus vidas.").imagen("gta_v.jpeg").precio(39.99).stock(90).build(),
                    // EXTRAS ELEGIDOS
                    Producto.builder().nombre("Red Dead Redemption 2").descripcion("Arthur Morgan y el ocaso del salvaje oeste.").imagen("red_dead_redemption_II.jpeg").precio(49.99).stock(70).build(),
                    Producto.builder().nombre("Elden Ring").descripcion("Explora las Tierras Intermedias y conviértete en Señor de las Eras.").imagen("elden_ring.jpeg").precio(59.99).stock(75).build(),
                    Producto.builder().nombre("Hollow Knight").descripcion("Un caballero insecto en las profundidades de Hallownest.").imagen("hollow.jpeg").precio(14.99).stock(100).build(),
                    Producto.builder().nombre("Spider-Man 2").descripcion("Peter Parker y Miles Morales enfrentan a Venom en Nueva York.").imagen("spiderman_2.jpeg").precio(59.99).stock(80).build()
                     ));

                System.out.println("✅ Catálogo de GodOfGames cargado con " + productoRepository.count() + " productos.");
            } else {
                System.out.println("✅ El catálogo ya tiene productos, no se recargó.");
            }
        };
    }
}