package com.example.cookcompileapp.repository

import android.net.Uri
import com.example.cookcompileapp.model.InstructionStep
import com.example.cookcompileapp.model.Recipe
import kotlin.math.floor

/**
 * Pure Kotlin recipe catalog generator and normalizer.
 * Expands seed Cookpad recipes into a full 1,050-recipe Nusantara catalog
 * with category-matched imagery, ingredients, and cooking steps.
 */
object RecipeCatalogGenerator {

    private data class DishTemplateGroup(
        val category: String,
        val subType: String,
        val type: String,
        val bases: List<String>,
        val modifiers: List<String>
    )

    private data class CandidateRecipe(
        val group: DishTemplateGroup,
        val base: String,
        val title: String
    )

    private data class GeneratedDetails(
        val description: String,
        val ingredients: List<String>,
        val instructions: List<String>
    )

    private val authors = listOf(
        "Bunda Atsaby", "Yulie Yuda Gunawan", "Aini Mama 2N 2R", "AnjaniGur", "Mama Akmal",
        "Maria Dian Andriana", "Emma Tumis", "Enggal", "Putra Adi Pamungkas", "Chef Devina Hermawan",
        "Dapur Umami", "Ibu Siti Rahma", "Dapoer Mak Wiwit", "Rani Kitchen", "Fitriani",
        "Dina Novita", "Siska Mandasari", "Dewi Sartika", "Bunda Nayla", "Mama Kenzo",
        "Novi Dapur Cantik", "Ayu Cooking", "Dapur Nusantara", "Eka Wahyuni", "Sri Rejeki"
    )

    private val fallbackSambalImages = listOf(
        "https://img-global.cpcdn.com/recipes/8c190d5eae70ad8d/1200x630cq80/photo.jpg",
        "https://img-global.cpcdn.com/recipes/5437bfd526b383a7/1200x630cq80/photo.jpg",
        "https://img-global.cpcdn.com/recipes/fe50fd846a0aa19e/1200x630cq80/photo.jpg"
    )

    private val dishTemplates = listOf(
        DishTemplateGroup(
            category = "Ayam",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Ayam Goreng Lengkuas", "Opor Ayam Gurih", "Ayam Bakar Taliwang", "Ayam Betutu Khas Bali",
                "Ayam Rica-Rica Manado", "Ayam Geprek Sambal Bawang", "Ayam Suwir Pedas Kemangi",
                "Gulai Ayam Padang", "Ayam Woku Belanga", "Ayam Bakar Bumbu Rujak", "Ayam Goreng Mentega",
                "Ayam Pop Khas Minang", "Ayam Serundeng Kelapa", "Ayam Tangkap Aceh", "Sate Ayam Madura",
                "Sop Ayam Jamur Jahe", "Ayam Kecap Pedas Manis", "Garang Asem Ayam", "Ayam Kalasan Legit",
                "Ayam Penyet Sambal Ijo"
            ),
            modifiers = listOf(
                "Spesial Rumahan", "Meresap Sempurna", "Pedas Nendang", "Bumbu Rempah", "Praktis Sat-Set",
                "Ala Resto", "Favorit Keluarga", "Harum Menggoda", "Empuk Juicy", "Sedap Mantap",
                "Bikin Lahap", "Warisan Dapur"
            )
        ),
        DishTemplateGroup(
            category = "Daging",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Rendang Daging Padang", "Rawon Daging Sapi", "Tongseng Sapi Kuah Gurih", "Empal Gentong Cirebon",
                "Dendeng Batokok Balado", "Sei Sapi Asap Sambal Luat", "Semur Daging Kentang", "Gulai Cincang Daging",
                "Soto Daging Madura", "Soto Betawi Kuah Susu", "Sate Maranggi Purwakarta", "Konro Bakar Makassar",
                "Krengsengan Daging Sapi", "Bistik Daging Jawa", "Daging Sapi Lada Hitam", "Sop Buntut Sapi",
                "Gulai Kambing Rempah", "Tongseng Kambing Tanpa Bau", "Sate Kambing Bumbu Kecap", "Gule Daging Kacang Merah"
            ),
            modifiers = listOf(
                "Empuk Juara", "Bumbu Medok", "Warisan Nenek", "Resep Tradisional", "Kuah Pekat",
                "Pedas Mantap", "Spesial Akhir Pekan", "Pilihan Keluarga", "Kaldu Sedap", "Paling Dicari",
                "Lezat Istimewa", "Harum Menggugah"
            )
        ),
        DishTemplateGroup(
            category = "Ikan & Seafood",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Ikan Bakar Jimbaran", "Gurame Asam Manis", "Cumi Saus Padang", "Udang Saus Tiram",
                "Cumi Hitam Gurih Manis", "Udang Goreng Mentega", "Pepes Ikan Mas Duri Lunak", "Bandeng Presto Sambal Terasi",
                "Sup Ikan Kakap Kuah Asam", "Kepiting Saus Lada Hitam", "Udang Bakar Madu", "Ikan Nila Bakar Rica",
                "Ikan Tongkol Balado", "Mangut Lele Asap", "Pindang Patin Palembang", "Sapo Tahu Seafood Komplit",
                "Cumi Asin Cabe Ijo", "Kerang Dara Rebus Saus Nanas", "Ikan Kembung Bumbu Kuning", "Tom Yum Seafood Nusantara"
            ),
            modifiers = listOf(
                "Segar Nikmat", "Bumbu Meresap", "Anti Amis", "Favorit Anak", "Garing di Luar",
                "Lembut di Dalam", "Kuah Hangat", "Bumbu Spesial", "Ala Pesisir", "Praktis Rumahan",
                "Lezat Menggoda", "Sedap Juara"
            )
        ),
        DishTemplateGroup(
            category = "Tahu & Tempe",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Bacem Tahu Tempe", "Orek Tempe Basah Manis", "Botok Tempe Teri Kelapa", "Mendoan Banyumas Renyah",
                "Tahu Jeletot Super Pedas", "Perkedel Tahu Daging", "Sambal Goreng Krecek Tempe", "Tahu Telur Surabaya",
                "Tempe Goreng Ketumbar", "Kari Tahu Santan Gurih", "Tahu Kipas Udang", "Tempe Penyet Kemangi",
                "Tahu Gejrot Cirebon", "Kering Tempe Kacang Teri", "Lodeh Tempe Cabe Hijau", "Tahu Bakso Kuah Bening"
            ),
            modifiers = listOf(
                "Murah Meriah", "Cepat Praktis", "Lauk Nasi Hangat", "Menu Harian", "Bergizi Tinggi",
                "Bumbu Mantap", "Favorit Keluarga", "Bikin Nambah", "Sedap Sederhana", "Bumbu Medok",
                "Lezat Ekonomis", "Nikmat Meresap"
            )
        ),
        DishTemplateGroup(
            category = "Nasi & Mie",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Nasi Goreng Kampung", "Nasi Liwet Sunda Teri Medan", "Nasi Uduk Betawi Komplit", "Nasi Kuning Manado",
                "Nasi Kebuli Kambing Harum", "Mie Goreng Jawa Tek-Tek", "Bakmi Godhog Jogja", "Mie Aceh Kuah Daging",
                "Kwetiau Goreng Seafood", "Bihun Goreng Kampung", "Nasi Goreng Gila", "Mie Celor Palembang",
                "Nasi Timbel Komplit", "Mie Ayam Bakso Rumahan", "Nasi Bakar Ayam Kemangi", "Kwetiau Kuah Sapi"
            ),
            modifiers = listOf(
                "Aroma Semerbak", "Pedas Gurih", "Porsi Kenyang", "Bumbu Rempah", "Resep Legendaris",
                "Sarapan Favorit", "Makan Malam Enak", "Sedap Nikmat", "Ala Kaki Lima", "Spesial Rumahan",
                "Bikin Ketagihan", "Paling Laris"
            )
        ),
        DishTemplateGroup(
            category = "Sayur & Sup",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Sayur Asem Segar Jakarta", "Sayur Lodeh Jawa Tengah", "Sop Bening Bayam Jagung", "Capcay Kuah Kental",
                "Tumis Kangkung Terasi", "Urap Sayuran Kelapa Parut", "Gado-Gado Siram Kacang", "Pecel Sayur Madiun",
                "Sayur Oyong Jamur Telur", "Tumis Buncis Daging Cincang", "Sop Kimlo Hangat Gurih", "Sayur Nangka Gulai Padang",
                "Tumis Tauge Tahu Asin", "Brongkos Daging Kacang Tolo", "Sop Brenebon Kacang Merah", "Plecing Kangkung Lombok"
            ),
            modifiers = listOf(
                "Penuh Vitamin", "Menu Sehat Harian", "Kuah Alami", "Renyah Lezat", "Resep Ibu",
                "Hangat di Badan", "Simpel Cepat", "Bergizi Seimbang", "Sedap Rumahan", "Favorit Keluarga",
                "Nikmat Alami", "Praktis Disajikan"
            )
        ),
        DishTemplateGroup(
            category = "Camilan",
            subType = "sweet",
            type = "Cemilan",
            bases = listOf(
                "Pisang Goreng Krispi Keju Coklat", "Martabak Manis Bangka Bersarang", "Onde-Onde Wijen Kacang Hijau",
                "Klepon Pandan Gula Merah", "Kue Cubit Lumer Setengah Matang", "Pisang Coklat Karamel Lumer",
                "Kue Lumpur Kentang Lembut", "Dadar Gulung Unti Kelapa", "Pancake Oat Pisang Madu",
                "Roti Bakar Bandung Manis", "Bolu Kukus Mekar Gula Merah", "Donat Kentang Empuk Tabur Gula"
            ),
            modifiers = listOf(
                "Lumer di Mulut", "Manis Legit Pas", "Teman Ngeteh Sore", "Jajanan Pasar", "Bikin Nagih",
                "Ide Jualan Untung", "Paling Laris", "Keluarga Happy", "Empuk Harum", "Praktis Rumahan",
                "Favorit Anak", "Sajian Spesial"
            )
        ),
        DishTemplateGroup(
            category = "Camilan",
            subType = "savory",
            type = "Cemilan",
            bases = listOf(
                "Risoles Mayo Daging Asap", "Lumpia Goreng Semarang", "Cireng Renyah Bumbu Rujak",
                "Bakwan Jagung Manis Gurih", "Pastel Renyah Isi Bihun Sayur", "Tahu Walik Krispi",
                "Singkong Keju Merekah", "Pempek Dos Tanpa Ikan", "Batagor Renyah Bumbu Kacang",
                "Siomay Bandung Ikan Tenggiri", "Bakwan Udang Sayur Renyah", "Martabak Telur Mini Kulit Lumpia"
            ),
            modifiers = listOf(
                "Kriuk Tahan Lama", "Teman Ngeteh Sore", "Jajanan Favorit", "Bikin Nagih", "Ide Jualan Untung",
                "Paling Laris", "Keluarga Happy", "Gurih Nikmat", "Praktis Rumahan", "Sajian Hangat",
                "Camilan Sore", "Favorit Semua"
            )
        ),
        DishTemplateGroup(
            category = "Minuman Segar",
            subType = "default",
            type = "Cemilan",
            bases = listOf(
                "Es Cendol Dawet Gula Merah", "Es Kuwut Melon Jeruk Nipis", "Es Teler Durian Santan", "Es Pisang Ijo Makassar",
                "Wedang Ronde Jahe Wangi", "Kolak Pisang Ubi Manis", "Es Kulkul Coklat Buah", "Sop Buah Kuah Susu",
                "Es Kelapa Muda Jeruk Peras", "Es Doger Komplit Bandung", "Jus Alpukat Kocok Coklat", "Es Lidah Buaya Selasih",
                "Wedang Jahe Susu Hangat", "Es Timun Serut Lemon", "Boba Brown Sugar Milk Tea", "Es Cincau Hijau Gula Jawa"
            ),
            modifiers = listOf(
                "Pelepas Dahaga", "Dingin Menyejukkan", "Manis Pas", "Favorit Buka Puasa", "Bikin Segar",
                "Sari Alami", "Sensasi Juara", "Harum Pandan", "Pilihan Sehat", "Nikmat Disajikan",
                "Resep Spesial", "Mudah Dibuat"
            )
        ),
        DishTemplateGroup(
            category = "Aneka Sambal",
            subType = "default",
            type = "Masakan",
            bases = listOf(
                "Sambal Matah Khas Bali", "Sambal Bawang Korek Pedas", "Sambal Terasi Matang", "Sambal Ijo Padang Asli",
                "Sambal Dabu-Dabu Manado", "Sambal Bajak Jawa Legit", "Sambal Mangga Muda", "Sambal Roa Khas Gorontalo",
                "Sambal Tempe Kemangi", "Sambal Gami Cumi Bontang", "Sambal Andaliman Khas Batak", "Sambal Belacan Segar"
            ),
            modifiers = listOf(
                "Pedas Nampol", "Aroma Menggoda", "Tahan Lama", "Minyak Kelapa Asli", "Wajib Dicoba",
                "Teman Lauk Juara", "Pecinta Pedas", "Bikin Habis Nasi", "Praktis Ulek", "Sedap Mantap",
                "Resep Rumahan", "Nikmat Menggugah"
            )
        )
    )

    fun hasDuplicateWords(base: String, modifier: String): Boolean {
        val wordRegex = Regex("[a-z0-9]+")
        val baseWords = wordRegex.findAll(base.lowercase())
            .map { it.value }
            .filter { it.length >= 3 }
            .toSet()
        return wordRegex.findAll(modifier.lowercase())
            .map { it.value }
            .filter { it.length >= 3 }
            .any { it in baseWords }
    }

    private fun buildRecipeDetails(
        category: String,
        subType: String,
        base: String,
        title: String
    ): GeneratedDetails {
        return when (category) {
            "Minuman Segar" -> {
                val isWarm = Regex("wedang|kolak", RegexOption.IGNORE_CASE).containsMatchIn(base)
                GeneratedDetails(
                    description = if (isWarm) {
                        "Resep $title yang hangat, harum rempah alami, dan manis pas di lidah. Cocok dinikmati saat sore hari atau cuaca sejuk bersama keluarga."
                    } else {
                        "Resep $title yang segar, manis alami, dan praktis dibuat di rumah. Cocok sebagai pelepas dahaga saat siang terik maupun sajian spesial keluarga."
                    },
                    ingredients = listOf(
                        "250 gram bahan utama ($base)",
                        "150 ml sirup gula merah atau gula pasir cair",
                        "200 ml santan matang atau susu cair segar",
                        "2 lembar daun pandan simpulkan",
                        "1 sdm biji selasih (rendam air hangat hingga mengembang)",
                        "100 gram nata de coco atau potongan buah segar",
                        "300 ml air matang bersih",
                        if (isWarm) "2 ruas jahe memarkan untuk aroma hangat" else "Es batu secukupnya"
                    ),
                    instructions = if (isWarm) {
                        listOf(
                            "Siapkan dan cuci bersih seluruh bahan utama untuk $base, potong sesuai selera.",
                            "Rebus air bersama gula merah, daun pandan, dan jahe memar hingga mendidih dan harum.",
                            "Masukkan isian utama lalu masak dengan api kecil hingga matang dan meresap.",
                            "Tambahkan santan atau susu sambil sesekali diaduk perlahan agar tidak pecah.",
                            "Angkat dan sajikan $base dalam mangkuk atau gelas selagi hangat."
                        )
                    } else {
                        listOf(
                            "Siapkan seluruh buah atau bahan isian untuk $base, cuci bersih lalu potong-potong sesuai selera.",
                            "Masak sirup gula merah atau gula pasir bersama daun pandan hingga harum, lalu dinginkan.",
                            "Tata bahan isian, nata de coco, biji selasih, dan es batu secukupnya ke dalam gelas atau mangkuk saji.",
                            "Tuangkan santan matang atau susu cair bersama air dingin dan larutan gula manis.",
                            "Aduk perlahan, koreksi tingkat kemanisan, dan sajikan $base selagi dingin."
                        )
                    }
                )
            }

            "Camilan" -> if (subType == "sweet") {
                GeneratedDetails(
                    description = "Resep $title yang manis, lembut, dan mudah dibuat di rumah. Sangat cocok untuk teman minum teh sore hari atau camilan keluarga.",
                    ingredients = listOf(
                        "250 gram bahan utama ($base)",
                        "150 gram tepung terigu protein sedang atau tepung ketan",
                        "75 gram gula pasir atau gula merah sisir halus",
                        "1 butir telur ayam suhu ruang",
                        "50 gram mentega atau margarin cair",
                        "150 ml susu cair atau santan hangat",
                        "1/2 sdt vanili bubuk dan sejumput garam",
                        "Topping keju parut, meses coklat, atau wijen sesuai selera"
                    ),
                    instructions = listOf(
                        "Siapkan dan takar seluruh bahan adonan untuk $base agar hasil lembut maksimal.",
                        "Campurkan bahan kering (tepung, gula, garam, vanili) dalam wadah lalu aduk rata.",
                        "Masukkan telur, susu atau santan, serta mentega cair sedikit demi sedikit sambil diaduk hingga licin tidak bergerindil.",
                        "Olah atau panggang/kukus/goreng adonan sesuai ciri khas $base dengan api sedang hingga matang dan harum.",
                        "Angkat, beri topping favorit, dan sajikan hangat bersama teh atau kopi."
                    )
                )
            } else {
                GeneratedDetails(
                    description = "Resep $title yang gurih, renyah, dan praktis dibuat sendiri di rumah. Pas untuk camilan santai maupun ide suguhan keluarga.",
                    ingredients = listOf(
                        "250 gram bahan utama ($base)",
                        "150 gram tepung terigu atau tepung tapioka sesuai sajian",
                        "3 siung bawang putih haluskan",
                        "2 batang daun bawang iris tipis",
                        "1 butir telur ayam",
                        "1 sdt garam halus dan 1/2 sdt lada bubuk",
                        "1 sdt kaldu jamur/ayam bubuk",
                        "150 ml air secukupnya dan minyak untuk menggoreng/mengukus"
                    ),
                    instructions = listOf(
                        "Siapkan bahan isian serta adonan untuk $base, potong atau cincang bahan pelengkap.",
                        "Campurkan tepung bersama bawang putih halus, daun bawang, garam, lada, kaldu, dan air hingga menjadi adonan pas.",
                        "Bentuk atau isi adonan sesuai selera hingga seluruh bahan habis.",
                        "Goreng dalam minyak panas api sedang hingga kuning keemasan (atau kukus hingga matang empuk).",
                        "Tiriskan dan sajikan $base selagi hangat bersama saus sambal atau bumbu kacang."
                    )
                )
            }

            "Aneka Sambal" -> GeneratedDetails(
                description = "Resep $title yang pedas segar dan bikin makan makin berselera. Cocok jadi pendamping aneka lauk goreng, bakar, dan nasi hangat.",
                ingredients = listOf(
                    "15 buah cabai rawit merah segar (sesuaikan selera pedas)",
                    "8 buah cabai merah keriting",
                    "6 siung bawang merah",
                    "3 siung bawang putih",
                    "1 buah tomat merah segar",
                    "1 sdt terasi bakar (opsional sesuai jenis sambal)",
                    "1 buah jeruk limau atau jeruk nipis",
                    "1 sdt garam halus dan 1/2 sdt gula merah sisir",
                    "4 sdm minyak goreng panas"
                ),
                instructions = listOf(
                    "Cuci bersih semua cabai, bawang merah, bawang putih, dan tomat untuk $base, lalu tiriskan.",
                    "Goreng sebentar bahan sambal hingga agak layu (atau iris segar tipis untuk sambal mentah).",
                    "Ulek cabai dan bawang bersama terasi bakar, garam, dan gula merah sesuai tingkat kehalusan yang diinginkan.",
                    "Siram dengan minyak goreng panas agar aroma sambal semakin harum dan tahan lama.",
                    "Tambahkan perasan air jeruk limau, koreksi rasa, dan sajikan bersama nasi hangat dan lauk."
                )
            )

            "Sayur & Sup" -> GeneratedDetails(
                description = "Resep $title yang kuahnya segar, kaya vitamin, dan mudah dimasak sehari-hari untuk menu sehat keluarga di rumah.",
                ingredients = listOf(
                    "400 gram sayuran segar pilihan ($base)",
                    "4 siung bawang merah iris tipis",
                    "3 siung bawang putih geprek atau cincang",
                    "1 liter air bersih atau kaldu ayam/sayur",
                    "1 batang daun bawang dan seledri iris",
                    "1 buah wortel atau jagung manis potong sesuai selera",
                    "1 sdt garam halus dan 1/2 sdt lada bubuk",
                    "1 sdt kaldu jamur dan 1 sdm minyak untuk menumis"
                ),
                instructions = listOf(
                    "Siangi dan cuci bersih semua sayuran untuk $base, potong sesuai ukuran suapan.",
                    "Tumis bawang merah dan bawang putih hingga harum (atau langsung masukkan ke air kaldu mendidih).",
                    "Masukkan sayuran bertekstur keras lebih dulu dan masak hingga setengah empuk.",
                    "Tambahkan sisa sayuran hijau, garam, lada, dan kaldu jamur lalu aduk perlahan.",
                    "Taburkan daun bawang dan seledri sesaat sebelum kompor dimatikan.",
                    "Koreksi rasa, angkat, dan sajikan hangat sebagai menu pendamping nasi."
                )
            )

            "Nasi & Mie" -> GeneratedDetails(
                description = "Resep $title yang gurih harum dan mengenyangkan, sangat cocok untuk menu sarapan cepat atau makan malam praktis bersama keluarga.",
                ingredients = listOf(
                    "400 gram nasi putih pulen atau mie rebus tiriskan ($base)",
                    "2 butir telur ayam kocok lepas",
                    "5 siung bawang merah dan 3 siung bawang putih cincang",
                    "2 sdm kecap manis dan 1 sdm saus tiram",
                    "50 gram sawi hijau atau kol iris",
                    "1 batang daun bawang iris tipis",
                    "1 sdt garam, 1/2 sdt lada bubuk, dan kaldu secukupnya",
                    "2 sdm minyak goreng atau margarin"
                ),
                instructions = listOf(
                    "Siapkan nasi atau mie serta seluruh bahan pelengkap untuk $base.",
                    "Panaskan minyak di wajan, buat telur orak-arik lalu sisihkan di tepi wajan.",
                    "Tumis bawang merah, bawang putih, dan bumbu halus hingga harum matang.",
                    "Masukkan bahan utama, sayuran, kecap manis, saus tiram, garam, dan lada.",
                    "Aduk rata dengan api sedang-besar hingga bumbu meresap sempurna.",
                    "Sajikan hangat dengan taburan bawang goreng, irisan timun, dan kerupuk."
                )
            )

            "Ikan & Seafood" -> GeneratedDetails(
                description = "Resep $title dengan daging segar tanpa bau amis, bumbu rempah meresap sempurna dan pas disajikan untuk makan siang atau malam.",
                ingredients = listOf(
                    "500 gram ikan atau seafood segar bersihkan ($base)",
                    "1 buah jeruk nipis untuk marinasi agar tidak amis",
                    "5 siung bawang merah dan 3 siung bawang putih haluskan",
                    "2 cm jahe dan 2 cm kunyit memarkan",
                    "2 lembar daun jeruk dan 1 batang serai geprek",
                    "3 buah cabai merah keriting sesuai selera",
                    "1 sdt garam, 1/2 sdt gula, dan 1/2 sdt lada bubuk",
                    "2 sdm minyak goreng dan 150 ml air bersih"
                ),
                instructions = listOf(
                    "Cuci bersih ikan atau seafood untuk $base, lumuri air jeruk nipis selama 10 menit lalu bilas.",
                    "Haluskan atau iris bawang merah, bawang putih, cabai, jahe, dan kunyit.",
                    "Tumis bumbu bersama daun jeruk dan serai hingga harum matang.",
                    "Masukkan bahan utama, aduk perlahan agar daging tidak hancur, lalu tuang air secukupnya.",
                    "Bumbui garam, gula, dan lada; masak hingga matang meresap.",
                    "Koreksi rasa, angkat, dan sajikan selagi hangat dengan nasi putih."
                )
            )

            "Daging" -> GeneratedDetails(
                description = "Resep $title dengan tekstur daging yang empuk dan bumbu rempah medok meresap, cocok untuk hidangan istimewa keluarga.",
                ingredients = listOf(
                    "500 gram daging segar potong sesuai selera ($base)",
                    "6 siung bawang merah dan 4 siung bawang putih haluskan",
                    "2 cm lengkuas dan 2 cm jahe memarkan",
                    "2 lembar daun salam, 2 lembar daun jeruk, dan 1 batang serai",
                    "2 sdm kecap manis atau santan sesuai jenis masakan",
                    "1 sdt ketumbar bubuk dan 1/2 sdt lada bubuk",
                    "1 sdt garam halus dan 1 sdt kaldu sapi",
                    "500 ml air bersih dan 2 sdm minyak goreng"
                ),
                instructions = listOf(
                    "Potong daging untuk $base melawan serat agar lebih cepat empuk saat dimasak.",
                    "Haluskan bawang merah, bawang putih, ketumbar, dan rempah sesuai kebutuhan.",
                    "Panaskan minyak, tumis bumbu halus bersama serai, daun salam, daun jeruk, dan lengkuas hingga harum.",
                    "Masukkan potongan daging, aduk hingga berubah warna.",
                    "Tuangkan air secukupnya, tutup wajan, dan masak dengan api kecil hingga daging empuk dan bumbu mengental.",
                    "Koreksi rasa, angkat, dan sajikan hangat bersama nasi putih."
                )
            )

            "Tahu & Tempe" -> GeneratedDetails(
                description = "Resep $title yang hemat, bergizi tinggi, dan nikmat dijadikan lauk nasi hangat setiap hari di rumah.",
                ingredients = listOf(
                    "400 gram tahu dan tempe segar potong sesuai selera ($base)",
                    "5 siung bawang merah dan 3 siung bawang putih",
                    "3 buah cabai rawit atau cabai hijau iris",
                    "2 lembar daun salam dan 1 cm lengkuas geprek",
                    "1 sdt ketumbar bubuk atau 2 sdm kecap manis",
                    "1 sdt garam halus dan 1 sdt kaldu jamur",
                    "150 ml air bersih dan minyak goreng secukupnya"
                ),
                instructions = listOf(
                    "Potong tahu dan tempe untuk $base, bisa dikukus atau digoreng setengah matang terlebih dahulu.",
                    "Tumis bawang merah, bawang putih, cabai, daun salam, dan lengkuas hingga wangi.",
                    "Masukkan tahu dan tempe, aduk rata perlahan agar tidak hancur.",
                    "Tambahkan air, garam, kaldu jamur, dan kecap atau bumbu pelengkap hingga meresap.",
                    "Masak hingga bumbu menyusut, koreksi rasa, dan sajikan hangat."
                )
            )

            else -> GeneratedDetails(
                description = "Resep $title yang gurih, empuk, dan bumbunya meresap hingga ke tulang. Menu favorit keluarga yang praktis dimasak di rumah.",
                ingredients = listOf(
                    "500 gram daging ayam potong bersih ($base)",
                    "5 siung bawang merah dan 3 siung bawang putih haluskan",
                    "2 cm kunyit, 2 cm jahe, dan 2 cm lengkuas memarkan",
                    "2 lembar daun salam, 2 lembar daun jeruk, dan 1 batang serai",
                    "1 sdt ketumbar bubuk dan 1/2 sdt lada bubuk",
                    "1 sdt garam halus dan 1 sdt kaldu ayam bubuk",
                    "250 ml air bersih dan 2 sdm minyak goreng"
                ),
                instructions = listOf(
                    "Cuci bersih potongan ayam untuk $base, lumuri sedikit jeruk nipis dan garam lalu bilas.",
                    "Haluskan bawang merah, bawang putih, kunyit, dan ketumbar hingga lembut.",
                    "Tumis bumbu halus bersama daun salam, daun jeruk, serai, dan lengkuas hingga harum matang.",
                    "Masukkan potongan ayam, aduk rata hingga berubah warna dan berbalut bumbu.",
                    "Tuangkan air, bumbui garam, lada, dan kaldu; masak dengan api sedang hingga ayam empuk dan kuah meresap.",
                    "Koreksi rasa, angkat (atau bakar/goreng sebentar sesuai varian), lalu sajikan hangat."
                )
            )
        }
    }

    fun ensureFullCatalog(seedRecipes: List<Recipe>, targetCount: Int = 1050): List<Recipe> {
        if (seedRecipes.size >= targetCount) return seedRecipes

        val categoryImagePools = mutableMapOf<String, MutableList<String>>()
        for (recipe in seedRecipes) {
            if (!recipe.imageUrl.startsWith("https://img-global.cpcdn.com/")) continue
            val poolCat = if (recipe.id == "26842283") "Camilan" else recipe.category
            val list = categoryImagePools.getOrPut(poolCat) { mutableListOf() }
            if (recipe.imageUrl !in list) list.add(recipe.imageUrl)
        }
        categoryImagePools.putIfAbsent("Aneka Sambal", fallbackSambalImages.toMutableList())

        val defaultPool = categoryImagePools["Ayam"]
            ?: categoryImagePools.values.firstOrNull()
            ?: fallbackSambalImages

        var seed = 20261116L
        fun nextRand(): Double {
            seed = (seed * 1664525L + 1013904223L) % 4294967296L
            return seed.toDouble() / 4294967296.0
        }

        val existingTitles = seedRecipes.map { it.title.lowercase() }.toMutableSet()
        val bucketsByCategory = dishTemplates.map { group ->
            val bucket = mutableListOf<CandidateRecipe>()
            for (base in group.bases) {
                for (mod in group.modifiers) {
                    if (hasDuplicateWords(base, mod)) continue
                    val title = "$base $mod"
                    if (title.lowercase() !in existingTitles) {
                        bucket.add(CandidateRecipe(group, base, title))
                    }
                }
            }
            for (i in bucket.size - 1 downTo 1) {
                val j = floor(nextRand() * (i + 1)).toInt()
                val tmp = bucket[i]
                bucket[i] = bucket[j]
                bucket[j] = tmp
            }
            bucket
        }

        val result = seedRecipes.toMutableList()
        var currentId = 30000000L
        val cursors = IntArray(bucketsByCategory.size)

        while (result.size < targetCount) {
            var addedInRound = false
            for (b in bucketsByCategory.indices) {
                if (result.size >= targetCount) break
                val bucket = bucketsByCategory[b]
                if (cursors[b] >= bucket.size) continue

                val item = bucket[cursors[b]++]
                val titleKey = item.title.lowercase()
                if (titleKey in existingTitles) continue
                existingTitles.add(titleKey)

                val pool = categoryImagePools[item.group.category]?.takeIf { it.isNotEmpty() } ?: defaultPool
                val img = pool[floor(nextRand() * pool.size).toInt()]
                val author = authors[floor(nextRand() * authors.size).toInt()]
                val rating = kotlin.math.round((4.6 + nextRand() * 0.4) * 10.0) / 10.0
                val reviews = 12 + floor(nextRand() * 88).toInt()
                val cookTime = "${10 + floor(nextRand() * 7).toInt() * 5} Menit"
                val portions = "${2 + floor(nextRand() * 4).toInt()} Porsi"
                val cal = if (item.group.category == "Minuman Segar") {
                    "${120 + floor(nextRand() * 140).toInt()} kkal"
                } else {
                    "${240 + floor(nextRand() * 280).toInt()} kkal"
                }

                val details = buildRecipeDetails(item.group.category, item.group.subType, item.base, item.title)

                result.add(
                    Recipe(
                        id = (currentId++).toString(),
                        title = item.title,
                        category = item.group.category,
                        type = item.group.type,
                        description = details.description,
                        imageUrl = img,
                        author = author,
                        sourceUrl = "https://cookpad.com/id/cari/${Uri.encode(item.base)}",
                        servings = portions,
                        cookTime = cookTime,
                        difficulty = if (nextRand() > 0.25) "Mudah" else "Sedang",
                        calories = cal,
                        rating = rating,
                        reviewCount = reviews,
                        isPopular = nextRand() > 0.85,
                        isFavorite = false,
                        ingredients = details.ingredients,
                        instructions = details.instructions.mapIndexed { idx, desc ->
                            InstructionStep(stepNumber = idx + 1, description = desc)
                        }
                    )
                )
                addedInRound = true
            }
            if (!addedInRound) break
        }

        return result
    }
}
