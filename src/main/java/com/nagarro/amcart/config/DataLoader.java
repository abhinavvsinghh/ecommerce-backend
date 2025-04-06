package com.nagarro.amcart.config;

import com.nagarro.amcart.model.Category;
import com.nagarro.amcart.model.Product;
import com.nagarro.amcart.repository.CategoryRepository;
import com.nagarro.amcart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            loadCategories();
        }
        
        if (productRepository.count() == 0) {
            loadProducts();
        }
    }
    
    private void loadCategories() {
        log.info("Loading categories...");
        
        // Create main categories
        Category menCategory = createCategory("Men", "Men's clothing and accessories", null, "men", 0);
        Category womenCategory = createCategory("Women", "Women's clothing and accessories", null, "women", 0);
        
        // Men's subcategories
        Category menClothing = createCategory("Clothing", "Men's clothing", menCategory.getId(), "men", 1);
        Category menAccessories = createCategory("Accessories", "Men's accessories", menCategory.getId(), "men", 1);
        Category menFootwear = createCategory("Footwear", "Men's footwear", menCategory.getId(), "men", 1);  // New subcategory
        
        // Men's clothing subcategories
        Category menTshirts = createCategory("T-Shirts", "Men's T-Shirts", menClothing.getId(), "men", 2);
        Category menCasualShirts = createCategory("Casual Shirts", "Men's Casual Shirts", menClothing.getId(), "men", 2);
        Category menFormalShirts = createCategory("Formal Shirts", "Men's Formal Shirts", menClothing.getId(), "men", 2);
        Category menJeans = createCategory("Jeans", "Men's Jeans", menClothing.getId(), "men", 2);
        Category menCasualTrousers = createCategory("Casual Trousers", "Men's Casual Trousers", menClothing.getId(), "men", 2);
        
        // Men's accessories subcategories
        // Category menWatches = createCategory("Watches", "Men's Watches", menAccessories.getId(), "men", 2);
        Category menSunglasses = createCategory("Sunglasses", "Men's Sunglasses", menAccessories.getId(), "men", 2);
        Category menBags = createCategory("Bags", "Men's Bags", menAccessories.getId(), "men", 2);
        
        // Men's footwear subcategories
        Category menSneakers = createCategory("Sneakers", "Men's Sneakers", menFootwear.getId(), "men", 2);
        Category menFormalShoes = createCategory("Formal Shoes", "Men's Formal Shoes", menFootwear.getId(), "men", 2);
        Category menSandals = createCategory("Sandals & Flip Flops", "Men's Sandals and Flip Flops", menFootwear.getId(), "men", 2);
        
        // Women's subcategories
        Category womenIndianWear = createCategory("Indian & Western Wear", "Women's Indian and Western Wear", womenCategory.getId(), "women", 1);
        Category womenWesternWear = createCategory("Western Wear", "Women's Western Wear", womenCategory.getId(), "women", 1);
        Category womenAccessories = createCategory("Accessories", "Women's accessories", womenCategory.getId(), "women", 1);
        
        // Women's Indian wear subcategories
        createCategory("Kurtas & Suits", "Women's Kurtas & Suits", womenIndianWear.getId(), "women", 2);
        createCategory("Kurtis & Tunics", "Women's Kurtis & Tunics", womenIndianWear.getId(), "women", 2);
        createCategory("Sarees", "Women's Sarees", womenIndianWear.getId(), "women", 2);
        
        // Women's Western wear subcategories
        createCategory("Dresses & Jumpsuits", "Women's Dresses & Jumpsuits", womenWesternWear.getId(), "women", 2);
        createCategory("Tops & Shirts", "Women's Tops & Shirts", womenWesternWear.getId(), "women", 2);
        createCategory("Jeans & Jeggings", "Women's Jeans & Jeggings", womenWesternWear.getId(), "women", 2);
        
        // Women's accessories subcategories
        // createCategory("Watches", "Women's Watches", womenAccessories.getId(), "women", 2);
        createCategory("Sunglasses", "Women's Sunglasses", womenAccessories.getId(), "women", 2);
        createCategory("Belts", "Women's Belts", womenAccessories.getId(), "women", 2);
        
        log.info("Categories loaded successfully");
    }
    
    private Category createCategory(String name, String description, String parentId, String gender, int level) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .parentId(parentId)
                .gender(gender)
                .level(level)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        return categoryRepository.save(category);
    }
    
    private void loadProducts() {
        log.info("Loading products...");
        
        // Get category references for Men's products
        Optional<Category> menTshirts = categoryRepository.findByName("T-Shirts");
        Optional<Category> menJeans = categoryRepository.findByName("Jeans");
        Optional<Category> menCasualShirts = categoryRepository.findByName("Casual Shirts");
        Optional<Category> menFormalShirts = categoryRepository.findByName("Formal Shirts");
        // Optional<Category> menWatches = categoryRepository.findByName("Watches");
        Optional<Category> menSneakers = categoryRepository.findByName("Sneakers");
        
        // Get category references for Women's products
        Optional<Category> womenDresses = categoryRepository.findByName("Dresses & Jumpsuits");
        Optional<Category> womenTops = categoryRepository.findByName("Tops & Shirts");
        Optional<Category> womenJeans = categoryRepository.findByName("Jeans & Jeggings");
        Optional<Category> womenKurtas = categoryRepository.findByName("Kurtas & Suits");
        
        // Add Men's T-shirts products
        if (menTshirts.isPresent()) {
            createProduct(
                "Classic Cotton T-Shirt",
                "A comfortable cotton t-shirt for everyday wear",
                new BigDecimal("29.99"),
                "Brand X",
                "Black",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/hero_75093e14-6264-41f5-b78f-553ca514c43c_765x.jpg?v=1738908883",
                    "https://thehouseofrare.com/cdn/shop/files/IMG_0183_1_bca355e6-9d4e-47f9-9207-30793f0a41b7_765x.jpg?v=1738908883"
                ),
                menTshirts.get().getId(),
                100,
                true,
                false,
                null
            );
            
            createProduct(
                "Graphic Print T-Shirt",
                "A stylish graphic print t-shirt for casual outings",
                new BigDecimal("34.99"),
                "Brand Y",
                "White",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/PONK-WHITE3278_765x.jpg?v=1740640681g",
                    "https://thehouseofrare.com/cdn/shop/files/PONK-WHITE3319_765x.jpg?v=1740640681"
                ),
                menTshirts.get().getId(),
                50,
                true,
                true,
                new BigDecimal("20")
            );
            
            createProduct(
                "Striped Polo T-Shirt",
                "Classic striped polo t-shirt for a smart casual look",
                new BigDecimal("45.99"),
                "Polo Club",
                "Navy Blue",
                Arrays.asList("M", "L", "XL", "XXL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/IMG_0038_18_765x.jpg?v=1738908881",
                    "https://thehouseofrare.com/cdn/shop/files/IMG_0015_27_43cf3735-54ea-4bbf-9e2a-fbc38153b26e_765x.jpg?v=1738908881"
                ),
                menTshirts.get().getId(),
                75,
                true,
                false,
                null
            );
            
            createProduct(
                "Sports Performance T-Shirt",
                "Moisture-wicking fabric for comfort during workouts",
                new BigDecimal("39.99"),
                "ActiveWear",
                "Red",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/turino-mens-polo-brown27624_fca36cc2-a1af-47f6-a603-621e511c352d_765x.jpg?v=1738908681",
                    "https://thehouseofrare.com/cdn/shop/files/TURINO-BROWN0102_765x.jpg?v=1738908681"
                ),
                menTshirts.get().getId(),
                60,
                true,
                true,
                new BigDecimal("15")
            );
        }
        
        // Add Men's Jeans products
        if (menJeans.isPresent()) {
            createProduct(
                "Slim Fit Jeans",
                "Comfortable slim fit jeans for a modern look",
                new BigDecimal("59.99"),
                "Denim Co",
                "Blue",
                Arrays.asList("30", "32", "34", "36"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/BENN-BLUE07247_765x.jpg?v=1740640281",
                    "https://thehouseofrare.com/cdn/shop/files/BENN-BLUE07259_765x.jpg?v=1740640281"
                ),
                menJeans.get().getId(),
                75,
                true,
                false,
                null
            );
            
            createProduct(
                "Distressed Skinny Jeans",
                "Modern distressed skinny jeans for a trendy look",
                new BigDecimal("69.99"),
                "Urban Denim",
                "Light Blue",
                Arrays.asList("28", "30", "32", "34"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/STARDARKNAVY__STARDARKNAVY_07458_765x.webp?v=1740640338",
                    "https://thehouseofrare.com/cdn/shop/files/STARDARKNAVY__STARDARKNAVY_07465_765x.webp?v=1740640339"
                ),
                menJeans.get().getId(),
                50,
                true,
                true,
                new BigDecimal("10")
            );
            
            createProduct(
                "Relaxed Fit Jeans",
                "Comfortable relaxed fit jeans for everyday wear",
                new BigDecimal("54.99"),
                "Comfort Denim",
                "Dark Blue",
                Arrays.asList("32", "34", "36", "38"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/JETTORDARKBLUE02457_765x.webp?v=1738907202",
                    "https://thehouseofrare.com/cdn/shop/files/JETTORDARKBLUE02464HERO-Copy_765x.webp?v=1738907202"
                ),
                menJeans.get().getId(),
                60,
                true,
                false,
                null
            );
        }
        
        // Add Men's Casual Shirts products
        if (menCasualShirts.isPresent()) {
            createProduct(
                "Checkered Casual Shirt",
                "Classic checkered pattern for a casual look",
                new BigDecimal("49.99"),
                "Style Co",
                "Red",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/john-mens-shirt-red5_ce0e6e7f-970b-4824-a424-cc23fbecb597_765x.webp?v=1739430055",
                    "https://thehouseofrare.com/cdn/shop/files/john-mens-shirt-red4_e61fc268-fed2-457c-baf1-a98a532732d5_765x.webp?v=1739430055"
                ),
                menCasualShirts.get().getId(),
                65,
                true,
                false,
                null
            );
            
            createProduct(
                "Linen Casual Shirt",
                "Lightweight linen shirt perfect for summer",
                new BigDecimal("55.99"),
                "Summer Essentials",
                "Beige",
                Arrays.asList("M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/lunet-beige-X-Design_765x.png?v=1741160606",
                    "https://thehouseofrare.com/cdn/shop/files/lunet-beige-0126_765x.jpg?v=1741160606"
                ),
                menCasualShirts.get().getId(),
                40,
                true,
                true,
                new BigDecimal("15")
            );
        }
        
        // Add Men's Formal Shirts products
        if (menFormalShirts.isPresent()) {
            createProduct(
                "Classic White Formal Shirt",
                "Crisp white formal shirt for business meetings",
                new BigDecimal("65.99"),
                "Business Elite",
                "White",
                Arrays.asList("15", "15.5", "16", "16.5", "17"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/RONAS-WHITE8451HERO_765x.jpg?v=1695817864",
                    "https://thehouseofrare.com/cdn/shop/files/RONAS-WHITE8429_765x.jpg?v=1709709594"
                ),
                menFormalShirts.get().getId(),
                80,
                true,
                false,
                null
            );
            
            createProduct(
                "Striped Formal Shirt",
                "Professional striped shirt for formal occasions",
                new BigDecimal("69.99"),
                "Executive Wear",
                "Blue & White",
                Arrays.asList("15", "15.5", "16", "16.5"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/products/IMG_0353_7a97645a-db51-4ddd-b590-a3d4ef3ea1a6_765x.jpg?v=1675413368",
                    "https://thehouseofrare.com/cdn/shop/products/IMG_0326_ee18a8f7-2575-4f2e-b3a5-603b25f5eaf7_765x.jpg?v=1676531676"
                ),
                menFormalShirts.get().getId(),
                55,
                true,
                false,
                null
            );
        }
        
        // Add Men's Watches products
        // if (menWatches.isPresent()) {
        //     createProduct(
        //         "Classic Analog Watch",
        //         "Elegant analog watch with leather strap",
        //         new BigDecimal("129.99"),
        //         "Timezone",
        //         "Brown",
        //         Arrays.asList("One Size"),
        //         Arrays.asList(
        //             "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcTCLZvniEkJf2Hx1ktT-thn9hdXKV2Ty6KBZTnhbzOPre5r80UdfYA6qWkQ7a0fQ55P4L7qmc8btFk5twk86FOVTgcPg1DDeOLeB3n_TougD1DC65ubAARxZg",
        //             "https://encrypted-tbn3.gstatic.com/shopping?q=tbn:ANd9GcQTD6UF0o-9qIeM-YozghX00s3Utw-c8woL8gJPh0s5c5UTdvNLkRGXcviMCoCunfXO-qT_EHjOnIGBna6BGqzNwGGalw4PAC-j09hdUovnnC8bbSaeTDP6"
        //         ),
        //         menWatches.get().getId(),
        //         30,
        //         true,
        //         false,
        //         null
        //     );
            
        //     createProduct(
        //         "Sports Digital Watch",
        //         "Multifunctional digital watch for active lifestyles",
        //         new BigDecimal("89.99"),
        //         "ActiveTime",
        //         "Black",
        //         Arrays.asList("One Size"),
        //         Arrays.asList(
        //             "https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcQFgLBdXTGj92Tbi4DA86Td0yXW8uJKoPjixmpz-1u1pfEkY0v4oS-VOXuzD8dum1p3rGusdWtgmGbeQoqRoqt8rCBLuxDVrZxUSj8v7kmYBeuxkw3nSKD2",
        //             "https://encrypted-tbn3.gstatic.com/shopping?q=tbn:ANd9GcQ5vFnxKguaSezTCuR2tl3CrY9eOmKFvKac7I4zyv66lS1JtafF8KWvjtRO9Sycy6hc6NbAfmMD-nJ9TgroS2eKgsN_vGGj"
        //         ),
        //         menWatches.get().getId(),
        //         45,
        //         true,
        //         true,
        //         new BigDecimal("20")
        //     );
        // }
        
        // Add Men's Sneakers products
        if (menSneakers.isPresent()) {
            createProduct(
                "Casual Canvas Sneakers",
                "Comfortable canvas sneakers for everyday wear",
                new BigDecimal("79.99"),
                "StreetStyle",
                "Black",
                Arrays.asList("7", "8", "9", "10", "11", "12"),
                Arrays.asList(
                    "https://images.meesho.com/images/products/451851918/7dop3_1200.jpg",
                    "https://images.meesho.com/images/products/451851918/zkwe1_1200.jpg"
                ),
                menSneakers.get().getId(),
                50,
                true,
                false,
                null
            );
            
            createProduct(
                "Running Performance Sneakers",
                "Designed for comfort and support during running",
                new BigDecimal("109.99"),
                "RunFast",
                "Blue & White",
                Arrays.asList("8", "9", "10", "11"),
                Arrays.asList(
                    "https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcR0L-788dE1KkrmJoCKRkRD_U-nJod4EBMD7FHfVqDk2DXkhkkoyUDI1Y-NAxdfvc_56dqVTD1lE8rJKwmjbI4BJZuu0rWuq6dqreHLC2-0JAjU0T07eo6HwdU",
                    "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcR6vx5aMAfyC_l4aLd1GXC9lUVu2eer8pIwQ_QcTe253QAv_Mo9xz-vPoOEKwRR8WS6Nd2M-3zdUIHqaKLXJq56z_1iEvcoC7kHxDAuOwEq5O8XAf9NLhKI"
                ),
                menSneakers.get().getId(),
                35,
                true,
                true,
                new BigDecimal("15")
            );
            
            createProduct(
                "High-Top Fashion Sneakers",
                "Trendy high-top sneakers for a stylish look",
                new BigDecimal("94.99"),
                "UrbanKicks",
                "White",
                Arrays.asList("7", "8", "9", "10", "11"),
                Arrays.asList(
                    "https://images.meesho.com/images/products/462272350/bk1up_1200.jpg",
                    "https://images.meesho.com/images/products/462272350/4csac_1200.jpg"
                ),
                menSneakers.get().getId(),
                40,
                true,
                false,
                null
            );
        }
        
        // Add Women's Dresses products
        if (womenDresses.isPresent()) {
            createProduct(
                "Floral Summer Dress",
                "A beautiful floral dress perfect for summer",
                new BigDecimal("49.99"),
                "Fashion Nova",
                "Multicolor",
                Arrays.asList("XS", "S", "M", "L"),
                Arrays.asList(
                    "https://5.imimg.com/data5/SELLER/Default/2023/10/354190737/ZB/XY/GW/185456460/86-1000x1000.jpg",
                    "https://5.imimg.com/data5/SELLER/Default/2023/10/354190759/PK/WD/GW/185456460/93-1000x1000.jpg"
                ),
                womenDresses.get().getId(),
                40,
                true,
                true,
                new BigDecimal("15")
            );
            
            createProduct(
                "Elegant Evening Gown",
                "Stunning evening gown for special occasions",
                new BigDecimal("129.99"),
                "Elegance",
                "Black",
                Arrays.asList("S", "M", "L"),
                Arrays.asList(
                    "https://drapedelight.com/wp-content/uploads/2024/12/hn3745.jpg",
                    "https://drapedelight.com/wp-content/uploads/2024/12/hn3745-a.jpg"
                ),
                womenDresses.get().getId(),
                25,
                true,
                false,
                null
            );
            
            createProduct(
                "Casual Maxi Dress",
                "Comfortable maxi dress for casual outings",
                new BigDecimal("59.99"),
                "DailyWear",
                "Blue",
                Arrays.asList("XS", "S", "M", "L", "XL"),
                Arrays.asList(
                    "https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcS3hnSW9Z6bYgRn8msOuQKZ4ZszcdIHup7VJo4yXvNZD2uwwUTxarmBhm7WqHTrNW8I0JmVZVME-06sxQalX0KmU60PX7RI9LqZo7FRWu2R_VcrNRMTGLBAXw",
                    "https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcSeXP_DFqKbO4V2LXmPvbQOK-TGkYYApMV5AbhSkaZEa0cCGi35zTha-mZEgW8j25xYcOx6XaqnnbLJG39tYXHqo5m3jNym18u2MFXNKeLM"
                ),
                womenDresses.get().getId(),
                50,
                true,
                true,
                new BigDecimal("10")
            );
        }
        
        // Add Women's Tops products
        if (womenTops.isPresent()) {
            createProduct(
                "Casual Blouse",
                "An elegant casual blouse for everyday wear",
                new BigDecimal("39.99"),
                "Style Co",
                "Pink",
                Arrays.asList("XS", "S", "M", "L"),
                Arrays.asList(
                    "https://5.imimg.com/data5/ANDROID/Default/2023/1/SO/BZ/KA/31650977/product-jpeg-1000x1000.jpg",
                    "https://5.imimg.com/data5/ANDROID/Default/2023/1/MB/JR/WF/31650977/product-jpeg-1000x1000.jpg"
                ),
                womenTops.get().getId(),
                60,
                true,
                false,
                null
            );
            
            createProduct(
                "Formal Button-Up Shirt",
                "Professional button-up shirt for work",
                new BigDecimal("45.99"),
                "Office Chic",
                "White",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/KRAFTT-WHITE-CC02058_765x.webp?v=1742983502",
                    "https://thehouseofrare.com/cdn/shop/files/KRAFTTWHITECC02100_765x.webp?v=1742983502"
                ),
                womenTops.get().getId(),
                45,
                true,
                false,
                null
            );
            
            createProduct(
                "Bohemian Print Top",
                "Stylish bohemian print top for a casual look",
                new BigDecimal("42.99"),
                "Boho Style",
                "Multicolor",
                Arrays.asList("S", "M", "L"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/XITOMULTI__XITOMULTI-33026_765x.webp?v=1732257644",
                    "https://thehouseofrare.com/cdn/shop/files/XITOMULTI__XITOMULTI-33023_765x.webp?v=1732257645"
                ),
                womenTops.get().getId(),
                35,
                true,
                true,
                new BigDecimal("15")
            );
        }
        
        // Add Women's Jeans products
        if (womenJeans.isPresent()) {
            createProduct(
                "High Waist Skinny Jeans",
                "Flattering high waist skinny jeans",
                new BigDecimal("54.99"),
                "Denim Diva",
                "Dark Blue",
                Arrays.asList("26", "28", "30", "32"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/Layer4_edcb3d3b-2573-45d3-aef7-497bae97da0f_765x.jpg?v=1722846280",
                    "https://thehouseofrare.com/cdn/shop/files/Untitled-1_0001_Layer6_480x.jpg?v=1722846280"
                ),
                womenJeans.get().getId(),
                55,
                true,
                false,
                null
            );
            
            createProduct(
                "Boyfriend Jeans",
                "Relaxed fit boyfriend jeans for a casual look",
                new BigDecimal("59.99"),
                "Casual Comfort",
                "Light Blue",
                Arrays.asList("28", "30", "32", "34"),
                Arrays.asList(
                    "https://thehouseofrare.com/cdn/shop/files/HERO_b8a73ba3-15e3-44ec-9808-4b883a3659f0_765x.jpg?v=1722846428",
                    "https://thehouseofrare.com/cdn/shop/files/4_ea83ee89-b440-4c33-ba9a-2e617b792a51_765x.jpg?v=1722846428"
                ),
                womenJeans.get().getId(),
                40,
                true,
                true,
                new BigDecimal("10")
            );
        }
        
        // Add Women's Kurtas products
        if (womenKurtas.isPresent()) {
            createProduct(
                "Embroidered Kurta Set",
                "Beautiful embroidered kurta with matching bottom",
                new BigDecimal("69.99"),
                "Ethnic Elegance",
                "Maroon",
                Arrays.asList("S", "M", "L", "XL"),
                Arrays.asList(
                    "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcTdPaxYNixFeUXxaGKuFIXUR6Zfq0hWwDKVVsB4hLczHgLXZhpS5t0-q986eoxAnUl6P6IeX6esmSkDEhF9vesq3RWanI6KwHg7RMRpf2A",
                    "https://encrypted-tbn1.gstatic.com/shopping?q=tbn:ANd9GcTe9lpifGq87ppPDruzZDviRErYiVU34hWIM89OgJNFZ_OunLL97xbnZS7j4KQ2PMi-lZPW_t7SL-E5YDz0YJaS4Ra5plP77DW61gPwyg"
                ),
                womenKurtas.get().getId(),
                30,
                true,
                false,
                null
            );
            
            createProduct(
                "Printed Cotton Kurta",
                "Comfortable printed cotton kurta for everyday wear",
                new BigDecimal("49.99"),
                "Daily Ethnic",
                "Blue",
                Arrays.asList("S", "M", "L", "XL", "XXL"),
                Arrays.asList(
                    "https://encrypted-tbn3.gstatic.com/shopping?q=tbn:ANd9GcTdMZwdSrpKw8PJckHuzuOG2xQXMQ3hUwuQw_d_U3WW_5LfXLgkucVHhL0AX4jLWt7k2HzOpI_Q7jRXcxYcAeXxL1IBvjTNOQOKhQ-nSV9UCR39wfANMP_Fdg",
                    "https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcTbryQ1ZY2iz7BQEMacyTLXQ-OOig9qJxRkoGTBgdtxVup3245fCj7a340fxhjaF2voyb6ukANAdijg3xXqN0iPMfagdZskGPrVWVBCseKFtfazP8TQ4P3N"
                ),
                womenKurtas.get().getId(),
                45,
                true,
                true,
                new BigDecimal("20")
            );
            
            createProduct(
                "Designer Party Wear Kurta",
                "Elegant designer kurta for special occasions",
                new BigDecimal("89.99"),
                "Celebration",
                "Golden",
                Arrays.asList("M", "L", "XL"),
                Arrays.asList(
                    "https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcRGAxvgCX4K6cBRH-0zBDzu6CwBP2BZT8-rJSQLnftWN7lRWdse_9Y7FQs6KeXhnompLTWzOkmnldS4nYktrYBjUEYlqxjJYsiKA-xNScwKeNE-xLon36lC",
                    "https://encrypted-tbn3.gstatic.com/shopping?q=tbn:ANd9GcRPQVkq8GcyPE7fB9xILmDUbHIMBvGzpaDZ8-dIyUgcL0bh3RMsjLS_zE8XYciL6leZ2EMznoLDj81HXqLVelAuUZUg66QI8mfL_QFpBOsPwClxWcH7DtHP"
                ),
                womenKurtas.get().getId(),
                25,
                true,
                false,
                null
            );
        }
        
        log.info("Products loaded successfully");
    }
    
    private void createProduct(
            String name,
            String description,
            BigDecimal price,
            String brand,
            String color,
            List<String> sizes,
            List<String> images,
            String categoryId,
            int stockQuantity,
            boolean inStock,
            boolean onSale,
            BigDecimal discountPercentage) {
        
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .brand(brand)
                .color(color)
                .sizes(sizes)
                .images(images)
                .categoryId(categoryId)
                .stockQuantity(stockQuantity)
                .inStock(inStock)
                .onSale(onSale)
                .discountPercentage(discountPercentage)
                .averageRating(0.0)  // Initialize with zero rating
                .reviewCount(0)      // Initialize with zero reviews
                .reviews(new ArrayList<>())  // Initialize with empty reviews list
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        
        productRepository.save(product);
    }
}