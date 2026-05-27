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
                    Producto.builder().nombre("Resident Evil 0").descripcion("El origen de la pesadilla en la mansión Spencer.").precio(29.99).stock(50).build(),
                    Producto.builder().nombre("Resident Evil 1 Remake").descripcion("La mansión Spencer rediseñada con gráficos modernos.").precio(29.99).stock(50).build(),
                    Producto.builder().nombre("Resident Evil 2 Remake").descripcion("Leon y Claire atrapados en Raccoon City.").precio(39.99).stock(60).build(),
                    Producto.builder().nombre("Resident Evil 3 Remake").descripcion("Jill Valentine huye del imparable Nemesis.").precio(39.99).stock(55).build(),
                    Producto.builder().nombre("Resident Evil 4 Remake").descripcion("Leon S. Kennedy en una misión de rescate en Europa.").precio(49.99).stock(70).build(),
                    Producto.builder().nombre("Resident Evil 5").descripcion("Chris Redfield en África enfrentando una nueva amenaza biológica.").precio(29.99).stock(45).build(),
                    Producto.builder().nombre("Resident Evil 6").descripcion("Múltiples campañas en un bioterrorismo global.").precio(29.99).stock(45).build(),
                    Producto.builder().nombre("Resident Evil 7: Biohazard").descripcion("Terror en primera persona en la mansión Baker.").precio(39.99).stock(60).build(),
                    Producto.builder().nombre("Resident Evil Village").descripcion("Ethan Winters en un misterioso pueblo europeo.").precio(49.99).stock(65).build(),
                    Producto.builder().nombre("Resident Evil Requiem").descripcion("El capítulo final de la saga Resident Evil.").precio(59.99).stock(80).build(),

                    // THE LAST OF US
                    Producto.builder().nombre("The Last of Us Part I").descripcion("Joel y Ellie atraviesan un mundo post-apocalíptico.").precio(49.99).stock(70).build(),
                    Producto.builder().nombre("The Last of Us Part II").descripcion("Ellie busca venganza en un mundo devastado.").precio(49.99).stock(65).build(),

                    // GOD OF WAR
                    Producto.builder().nombre("God of War (2018)").descripcion("Kratos y Atreus en la mitología nórdica.").precio(39.99).stock(75).build(),
                    Producto.builder().nombre("God of War Ragnarok").descripcion("El Ragnarok se acerca y Kratos debe enfrentarlo.").precio(59.99).stock(80).build(),

                    // GHOST OF TSUSHIMA
                    Producto.builder().nombre("Ghost of Tsushima").descripcion("Jin Sakai defiende Tsushima de la invasión mongola.").precio(49.99).stock(60).build(),
                    Producto.builder().nombre("Ghost of Tsushima Director's Cut").descripcion("Edición completa con la isla Iki y modo Legends.").precio(59.99).stock(55).build(),

                    // NEED FOR SPEED
                    Producto.builder().nombre("Need for Speed Most Wanted").descripcion("Conviértete en el corredor más buscado de Rockport.").precio(29.99).stock(50).build(),
                    Producto.builder().nombre("Need for Speed Heat").descripcion("Carreras legales de día, ilegales de noche en Palm City.").precio(39.99).stock(50).build(),
                    Producto.builder().nombre("Need for Speed Unbound").descripcion("Estilo urbano y arte callejero en Lakeshore City.").precio(49.99).stock(45).build(),

                    // GTA
                    Producto.builder().nombre("Grand Theft Auto IV").descripcion("Niko Bellic busca el sueño americano en Liberty City.").precio(29.99).stock(55).build(),
                    Producto.builder().nombre("Grand Theft Auto V").descripcion("Tres criminales en los Ángeles en el mayor golpe de sus vidas.").precio(39.99).stock(90).build(),
                    Producto.builder().nombre("Grand Theft Auto VI").descripcion("El regreso a Vice City con la primera protagonista femenina de GTA.").precio(69.99).stock(100).build(),

                    // EXTRAS ELEGIDOS
                    Producto.builder().nombre("Red Dead Redemption 2").descripcion("Arthur Morgan y el ocaso del salvaje oeste.").precio(49.99).stock(70).build(),
                    Producto.builder().nombre("The Witcher 3: Wild Hunt").descripcion("Geralt de Rivia en la búsqueda de Ciri por un mundo en guerra.").precio(39.99).stock(65).build(),
                    Producto.builder().nombre("Cyberpunk 2077").descripcion("V busca la inmortalidad en la megalópolis Night City.").precio(49.99).stock(70).build(),
                    Producto.builder().nombre("Elden Ring").descripcion("Explora las Tierras Intermedias y conviértete en Señor de las Eras.").precio(59.99).stock(75).build(),
                    Producto.builder().nombre("Hollow Knight").descripcion("Un caballero insecto en las profundidades de Hallownest.").precio(14.99).stock(100).build(),
                    Producto.builder().nombre("Hades").descripcion("Zagreus intenta escapar del inframundo en este roguelike épico.").precio(24.99).stock(90).build(),
                    Producto.builder().nombre("Spider-Man 2").descripcion("Peter Parker y Miles Morales enfrentan a Venom en Nueva York.").precio(59.99).stock(80).build(),
                    Producto.builder().nombre("Horizon Forbidden West").descripcion("Aloy explora el oeste prohibido lleno de máquinas salvajes.").precio(49.99).stock(65).build()
                ));

                System.out.println("✅ Catálogo de GodOfGames cargado con " + productoRepository.count() + " productos.");
            } else {
                System.out.println("✅ El catálogo ya tiene productos, no se recargó.");
            }
        };
    }
}